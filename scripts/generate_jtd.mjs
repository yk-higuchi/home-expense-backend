#!/usr/bin/env node
import { readFileSync, writeFileSync } from "node:fs";
import { parse } from "yaml";

const USAGE = "使い方: generate_jtd.mjs <openapi.yaml> <出力先.json>";
const REF_PREFIX = "#/components/schemas/";
const HTTP_METHODS = [
	"get",
	"put",
	"post",
	"delete",
	"options",
	"head",
	"patch",
	"trace",
	"query",
];

const [specPath, outputPath] = process.argv.slice(2);
if (!specPath || !outputPath) {
	console.error(USAGE);
	process.exit(2);
}

const spec = parse(readFileSync(specPath, "utf8"));
const schemas = spec.components?.schemas ?? {};

const fail = (where, message) => {
	throw new Error(`${where}: ${message}`);
};

const refName = (ref, where) => {
	if (!ref.startsWith(REF_PREFIX))
		fail(where, `対応していない参照です: ${ref}`);
	const name = ref.slice(REF_PREFIX.length);
	if (!(name in schemas)) fail(where, `参照先のスキーマがありません: ${ref}`);
	return name;
};

const resolve = (object, section) => {
	if (object?.$ref === undefined) return object;
	return spec.components[section][object.$ref.split("/").pop()];
};

const schemaRefsIn = (content) =>
	Object.values(content ?? {})
		.map((media) => media.schema?.$ref)
		.filter((ref) => ref !== undefined);

const roots = new Map();
const addRoot = (ref, mode, where) => {
	const name = refName(ref, where);
	const current = roots.get(name);
	if (current !== undefined && current !== mode) {
		fail(
			name,
			"リクエストとレスポンスの両方で使われているため、追加のプロパティの扱いを決められません",
		);
	}
	roots.set(name, mode);
};

for (const [path, item] of Object.entries(spec.paths ?? {})) {
	for (const method of HTTP_METHODS) {
		const operation = item[method];
		if (operation === undefined) continue;
		const where = `${method.toUpperCase()} ${path}`;
		const body = resolve(operation.requestBody, "requestBodies");
		for (const ref of schemaRefsIn(body?.content))
			addRoot(ref, "request", where);
		for (const response of Object.values(operation.responses ?? {})) {
			for (const ref of schemaRefsIn(resolve(response, "responses").content))
				addRoot(ref, "response", where);
		}
	}
}

const convert = (schema, mode, where, stack) => {
	if (schema.$ref !== undefined) {
		const name = refName(schema.$ref, where);
		if (stack.includes(name))
			fail(
				where,
				`再帰的な参照には対応していません: ${[...stack, name].join(" → ")}`,
			);
		return convert(schemas[name], mode, name, [...stack, name]);
	}
	for (const keyword of ["oneOf", "anyOf", "allOf", "not"]) {
		if (schema[keyword] !== undefined)
			fail(where, `${keyword} には対応していません`);
	}
	const types = Array.isArray(schema.type) ? schema.type : [schema.type];
	const nullable = types.includes("null");
	const type = types.filter((t) => t !== "null");
	if (type.length !== 1)
		fail(where, `type を 1 つに決められません: ${JSON.stringify(schema.type)}`);
	const result = convertType(type[0], schema, mode, where, stack);
	return nullable ? { ...result, nullable: true } : result;
};

const convertType = (type, schema, mode, where, stack) => {
	switch (type) {
		case "string":
			return schema.enum !== undefined
				? { enum: [...schema.enum] }
				: { type: "string" };
		case "boolean":
			return { type: "boolean" };
		case "number":
			return { type: "float64" };
		case "integer":
			return { type: schema.format === "int32" ? "int32" : "float64" };
		case "array":
			if (schema.items === undefined) fail(where, "items がありません");
			return { elements: convert(schema.items, mode, `${where}[]`, stack) };
		case "object":
			return convertObject(schema, mode, where, stack);
		default:
			return fail(where, `対応していない type です: ${type}`);
	}
};

const convertObject = (schema, mode, where, stack) => {
	if (typeof schema.additionalProperties === "object")
		fail(where, "値の型を指定した additionalProperties には対応していません");
	const required = new Set(schema.required ?? []);
	const properties = {};
	const optionalProperties = {};
	for (const [key, value] of Object.entries(schema.properties ?? {})) {
		(required.has(key) ? properties : optionalProperties)[key] = convert(
			value,
			mode,
			`${where}.${key}`,
			stack,
		);
	}
	for (const key of required) {
		if (!(key in properties))
			fail(where, `required の ${key} が properties にありません`);
	}
	const result = {};
	if (Object.keys(properties).length > 0) result.properties = properties;
	if (Object.keys(optionalProperties).length > 0)
		result.optionalProperties = optionalProperties;
	if (Object.keys(result).length === 0) result.properties = {};
	if (mode === "response" || schema.additionalProperties === true)
		result.additionalProperties = true;
	return result;
};

const output = {};
for (const name of [...roots.keys()].sort()) {
	output[name] = convert(schemas[name], roots.get(name), name, [name]);
}
writeFileSync(outputPath, `${JSON.stringify(output, null, "\t")}\n`);
