-- Chạy sau khi restore lên Server6 để user tp3_readonly có quyền SELECT

DO $$
DECLARE
  r RECORD;
BEGIN
  -- cấp quyền dùng schema public
  EXECUTE 'GRANT USAGE ON SCHEMA public TO tp3_readonly';

  -- cấp SELECT cho toàn bộ table trong public
  FOR r IN (SELECT tablename FROM pg_tables WHERE schemaname = 'public')
  LOOP
    EXECUTE format('GRANT SELECT ON TABLE public.%I TO tp3_readonly', r.tablename);
  END LOOP;
END $$;

ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT ON TABLES TO tp3_readonly;
