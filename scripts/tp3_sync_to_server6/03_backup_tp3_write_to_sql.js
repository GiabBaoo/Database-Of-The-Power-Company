require("dotenv").config();
const fs = require("fs");
const path = require("path");
const { Client } = require("pg");

function qIdent(name) {
  return `"${String(name).replaceAll('"', '""')}"`;
}

function formatLiteral(value) {
  if (value === null || value === undefined) return "NULL";
  if (typeof value === "number") return Number.isFinite(value) ? String(value) : "NULL";
  if (typeof value === "boolean") return value ? "TRUE" : "FALSE";
  if (value instanceof Date) return `'${value.toISOString().replace("T", " ").replace("Z", "+00")}'`;
  // Buffer (bytea)
  if (Buffer.isBuffer(value)) return `'\\x${value.toString("hex")}'`;
  // JSON / objects
  if (typeof value === "object") return `'${JSON.stringify(value).replaceAll("'", "''")}'`;
  // string
  return `'${String(value).replaceAll("'", "''")}'`;
}

async function main() {
  const cfg = {
    user: process.env.DB_User3,
    password: process.env.DB_Password3,
    host: process.env.DB_Server3,
    port: parseInt(process.env.DB_Server3_Port || "5432", 10),
    database: process.env.DB_Name3 || "postgres",
    ssl: { rejectUnauthorized: false },
  };

  const outDir = __dirname;
  const outFile = path.join(outDir, "tp3_write_backup.sql");

  const client = new Client(cfg);
  await client.connect();

  const ws = fs.createWriteStream(outFile, { encoding: "utf8" });
  const write = (s) => ws.write(s);

  write("-- TP3_WRITE backup (logical SQL)\n");
  write(`-- Generated at: ${new Date().toISOString()}\n`);
  write("-- Source: Supabase PostgreSQL (TP3_WRITE)\n\n");
  write("BEGIN;\n");
  write("SET statement_timeout = 0;\n");
  write("SET lock_timeout = 0;\n");
  write("SET idle_in_transaction_session_timeout = 0;\n");
  write("SET client_encoding = 'UTF8';\n");
  write("SET standard_conforming_strings = on;\n\n");

  // 1) Schema: use pg_dump-style via pg_get_* for tables/constraints/indexes
  // Keep it pragmatic: dump CREATE TABLE for public tables; then dump data.
  const tablesRes = await client.query(
    `
    SELECT table_name
    FROM information_schema.tables
    WHERE table_schema = 'public' AND table_type = 'BASE TABLE'
    ORDER BY table_name;
    `
  );
  const tables = tablesRes.rows.map((r) => r.table_name);

  write("-- =====================\n");
  write("-- Schema (public tables)\n");
  write("-- =====================\n\n");

  for (const t of tables) {
    const ddlRes = await client.query(
      `
      SELECT
        'CREATE TABLE ' || quote_ident(n.nspname) || '.' || quote_ident(c.relname) || E' (\\n' ||
        string_agg(
          '  ' || quote_ident(a.attname) || ' ' ||
          pg_catalog.format_type(a.atttypid, a.atttypmod) ||
          CASE WHEN a.attnotnull THEN ' NOT NULL' ELSE '' END ||
          CASE WHEN ad.adbin IS NOT NULL THEN ' DEFAULT ' || pg_get_expr(ad.adbin, ad.adrelid) ELSE '' END
        , E',\\n' ORDER BY a.attnum) ||
        E'\\n);' AS ddl
      FROM pg_class c
      JOIN pg_namespace n ON n.oid = c.relnamespace
      JOIN pg_attribute a ON a.attrelid = c.oid
      LEFT JOIN pg_attrdef ad ON ad.adrelid = c.oid AND ad.adnum = a.attnum
      WHERE n.nspname = 'public'
        AND c.relname = $1
        AND c.relkind = 'r'
        AND a.attnum > 0
        AND NOT a.attisdropped
      GROUP BY n.nspname, c.relname;
      `,
      [t]
    );

    if (ddlRes.rows[0]?.ddl) {
      write(`DROP TABLE IF EXISTS public.${qIdent(t)} CASCADE;\n`);
      write(ddlRes.rows[0].ddl + "\n\n");
    }
  }

  write("-- =====================\n");
  write("-- Data (public tables)\n");
  write("-- =====================\n\n");

  for (const t of tables) {
    const rowsRes = await client.query(`SELECT * FROM public.${qIdent(t)};`);
    const rows = rowsRes.rows;
    if (rows.length === 0) continue;

    const colsRes = await client.query(
      `
      SELECT column_name
      FROM information_schema.columns
      WHERE table_schema='public' AND table_name=$1
      ORDER BY ordinal_position;
      `,
      [t]
    );
    const cols = colsRes.rows.map((r) => r.column_name);
    const colList = cols.map(qIdent).join(", ");

    write(`-- Table: public.${qIdent(t)}\n`);
    write(`DELETE FROM public.${qIdent(t)};\n`);

    for (const row of rows) {
      const values = cols.map((c) => formatLiteral(row[c]));
      write(`INSERT INTO public.${qIdent(t)} (${colList}) VALUES (${values.join(", ")});\n`);
    }
    write("\n");
  }

  write("COMMIT;\n");

  await new Promise((resolve, reject) => {
    ws.end(() => resolve());
    ws.on("error", reject);
  });
  await client.end();

  // Print only path/size (avoid printing secrets)
  const stat = fs.statSync(outFile);
  console.log(`✅ Backup created: ${outFile}`);
  console.log(`   Size: ${stat.size} bytes`);
}

main().catch((err) => {
  console.error("❌ Backup failed:", err?.message || err);
  process.exitCode = 1;
});

