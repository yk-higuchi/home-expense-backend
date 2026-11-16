#!/usr/bin/env node
import { readFileSync } from "node:fs";
import Ajv2020 from "ajv/dist/2020.js";
import addFormats from "ajv-formats";
import { parse } from "yaml";

const USAGE =
	"使い方: validate_json_schema_exchanges.mjs <openapi.yaml> <openapi.schema.json> <exchanges.jsonl>";
const REJECTED_ON_PURPOSE = new Set([400, 406, 412, 415, 422, 428]);

const [specPath, schemaPath, exchangesPath] = process.argv.slice(2);
if (!specPath || !schemaPath || !exchangesPath) {
	console.error(USAGE);
	process.exit(2);
}

const spec = parse(readFileSync(specPath, "utf8"));
const document = JSON.parse(readFileSync(schemaPath, "utf8"));
const exchanges = readFileSync(exchangesPath, "utf8")
	.split("\n")
	.filter((line) => line.trim() !== "")
	.map((line) => JSON.parse(line));

const ajv = new Ajv2020({ strict: true, allErrors: true });
addFormats(ajv, ["date", "uuid", "uri", "int32", "int64"]);
ajv.addSchema(document);
const validatorFor = (name) => {
	const validate = ajv.getSchema(`${document.$id}#/$defs/${name}`);
	if (validate === undefined) throw new Error(`スキーマがありません: ${name}`);
	return validate;
};

const routes = Object.entries(spec.paths).map(([path, item]) => ({
	item,
	pattern: new RegExp(
		`^${path.replace(/[.*+?^$()|[\]\\]/g, "\\$&").replace(/\{[^}]+\}/g, "[^/]+")}$`,
	),
}));

const resolve = (object, section) =>
	object?.$ref === undefined
		? object
		: spec.components[section][object.$ref.split("/").pop()];

const schemaName = (content, contentType) => {
	const media = content?.[(contentType ?? "").split(";")[0].trim()];
	return media?.schema?.$ref?.split("/").pop();
};

let checked = 0;
let failures = 0;
const report = (exchange, message) => {
	failures += 1;
	const url = new URL(exchange.uri);
	console.log(
		`--- ${exchange.method} ${url.pathname}${url.search} -> ${exchange.status} (${exchange.test})`,
	);
	console.log(`    ${message}`);
};

for (const exchange of exchanges) {
	const url = new URL(exchange.uri);
	const route = routes.find(({ pattern }) => pattern.test(url.pathname));
	const operation = route?.item[exchange.method.toLowerCase()];
	if (operation === undefined) continue;

	const response = resolve(
		operation.responses?.[String(exchange.status)],
		"responses",
	);
	const responseSchema = schemaName(
		response?.content,
		exchange.response_content_type,
	);
	if (responseSchema !== undefined) {
		checked += 1;
		const validate = validatorFor(responseSchema);
		if (!validate(JSON.parse(exchange.response_body ?? "null"))) {
			report(
				exchange,
				`レスポンスが ${responseSchema} と一致しません: ${ajv.errorsText(validate.errors)}`,
			);
		}
	}

	const body = resolve(operation.requestBody, "requestBodies");
	const requestSchema = schemaName(
		body?.content,
		exchange.request_content_type,
	);
	if (
		requestSchema !== undefined &&
		exchange.request_body &&
		!REJECTED_ON_PURPOSE.has(exchange.status)
	) {
		checked += 1;
		const validate = validatorFor(requestSchema);
		if (!validate(JSON.parse(exchange.request_body))) {
			report(
				exchange,
				`リクエストが ${requestSchema} と一致しません: ${ajv.errorsText(validate.errors)}`,
			);
		}
	}
}

console.log(`JSON Schema で ${checked} 件を照合: 失敗 ${failures} 件`);
process.exit(failures > 0 ? 1 : 0);
