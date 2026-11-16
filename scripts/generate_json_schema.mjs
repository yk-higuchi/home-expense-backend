#!/usr/bin/env node
import { readFileSync, writeFileSync } from "node:fs";
import { parse } from "yaml";

const USAGE = "使い方: generate_json_schema.mjs <openapi.yaml> <出力先.json>";
const SCHEMA_ID = "urn:home-expense:openapi";
const DIALECT = "https://json-schema.org/draft/2020-12/schema";
const REF_PREFIX = "#/components/schemas/";
const OPENAPI_ONLY_KEYWORDS = [
	"discriminator",
	"xml",
	"externalDocs",
	"example",
];

const [specPath, outputPath] = process.argv.slice(2);
if (!specPath || !outputPath) {
	console.error(USAGE);
	process.exit(2);
}

const spec = parse(readFileSync(specPath, "utf8"));
const schemas = spec.components?.schemas ?? {};

if (
	spec.jsonSchemaDialect !== undefined &&
	spec.jsonSchemaDialect !== DIALECT
) {
	throw new Error(
		`対応していない jsonSchemaDialect です: ${spec.jsonSchemaDialect}`,
	);
}

const convert = (node, where) => {
	if (Array.isArray(node))
		return node.map((item, i) => convert(item, `${where}/${i}`));
	if (node === null || typeof node !== "object") return node;
	const result = {};
	for (const [key, value] of Object.entries(node)) {
		const path = `${where}/${key}`;
		const isKeyword = !where.endsWith("/properties");
		if (isKeyword && OPENAPI_ONLY_KEYWORDS.includes(key)) {
			throw new Error(
				`${path}: OpenAPI 独自のキーワードは JSON Schema に出力できません`,
			);
		}
		if (isKeyword && key === "$ref") {
			if (typeof value !== "string" || !value.startsWith(REF_PREFIX)) {
				throw new Error(`${path}: 対応していない参照です: ${value}`);
			}
			const name = value.slice(REF_PREFIX.length);
			if (!(name in schemas))
				throw new Error(`${path}: 参照先のスキーマがありません: ${value}`);
			result[key] = `#/$defs/${name}`;
			continue;
		}
		result[key] = convert(value, path);
	}
	return result;
};

const defs = Object.fromEntries(
	Object.keys(schemas)
		.sort()
		.map((name) => [
			name,
			convert(schemas[name], `#/components/schemas/${name}`),
		]),
);

const document = {
	$schema: DIALECT,
	$id: SCHEMA_ID,
	title: spec.info?.title,
	description:
		"docs/openapi.yaml の components.schemas から生成した JSON Schema です。手で編集しないでください。",
	$defs: defs,
};

writeFileSync(outputPath, `${JSON.stringify(document, null, "\t")}\n`);
