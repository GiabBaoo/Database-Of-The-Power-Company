-- TP3_WRITE backup (logical SQL)
-- Generated at: 2026-04-09T14:11:44.374Z
-- Source: Supabase PostgreSQL (TP3_WRITE)

BEGIN;
SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;

-- =====================
-- Schema (public tables)
-- =====================

DROP TABLE IF EXISTS public."asg_hoz2" CASCADE;
CREATE TABLE public.asg_hoz2 (
  eno character varying(10),
  pno character varying(10),
  resp character varying(50),
  dur integer
);

DROP TABLE IF EXISTS public."asg_v2" CASCADE;
CREATE TABLE public.asg_v2 (
  eno character varying(10) NOT NULL,
  pno character varying(10) NOT NULL,
  dur integer
);

DROP TABLE IF EXISTS public."chinhanh" CASCADE;
CREATE TABLE public.chinhanh (
  macn character varying(20) NOT NULL,
  tencn character varying(255) NOT NULL,
  thanhpho character varying(100)
);

DROP TABLE IF EXISTS public."hoadon" CASCADE;
CREATE TABLE public.hoadon (
  sohdn character varying(20) NOT NULL,
  thang integer,
  nam integer,
  sohd character varying(20),
  manv character varying(20),
  sotien integer
);

DROP TABLE IF EXISTS public."hopdong" CASCADE;
CREATE TABLE public.hopdong (
  sohd character varying(20) NOT NULL,
  ngayky date DEFAULT CURRENT_DATE,
  makh character varying(20),
  sodienke character varying(50),
  kwdinhmuc integer,
  dongiakw integer,
  ispaid boolean DEFAULT false
);

DROP TABLE IF EXISTS public."khachhang" CASCADE;
CREATE TABLE public.khachhang (
  makh character varying(20) NOT NULL,
  tenkh character varying(255) NOT NULL,
  macn character varying(20)
);

DROP TABLE IF EXISTS public."lichsuchuyencongtac" CASCADE;
CREATE TABLE public.lichsuchuyencongtac (
  id integer NOT NULL DEFAULT nextval('lichsuchuyencongtac_id_seq'::regclass),
  manv character varying(20),
  ngaychuyen timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
  macncu character varying(20),
  macnmoi character varying(20),
  makh character varying(20)
);

DROP TABLE IF EXISTS public."nhanvien" CASCADE;
CREATE TABLE public.nhanvien (
  manv character varying(20) NOT NULL,
  hoten character varying(255) NOT NULL,
  macn character varying(20),
  password character varying(100),
  role character varying(50)
);

DROP TABLE IF EXISTS public."orders2" CASCADE;
CREATE TABLE public.orders2 (
  custid character varying(10) NOT NULL,
  prodid character varying(10) NOT NULL,
  orderdate character varying(20)
);

DROP TABLE IF EXISTS public."orders2hoz" CASCADE;
CREATE TABLE public.orders2hoz (
  custid character varying(10) NOT NULL,
  prodid character varying(10) NOT NULL,
  qty integer,
  orderdate character varying(20)
);

DROP TABLE IF EXISTS public."users" CASCADE;
CREATE TABLE public.users (
  manv character varying(20) NOT NULL,
  email character varying(200),
  password character varying(512),
  role character varying(30),
  salt character varying(100)
);

-- =====================
-- Data (public tables)
-- =====================

-- Table: public."asg_hoz2"
DELETE FROM public."asg_hoz2";
INSERT INTO public."asg_hoz2" ("eno", "pno", "resp", "dur") VALUES ('E3', 'P3', 'Consultant', 10);
INSERT INTO public."asg_hoz2" ("eno", "pno", "resp", "dur") VALUES ('E3', 'P4', 'Engineer', 48);

-- Table: public."asg_v2"
DELETE FROM public."asg_v2";
INSERT INTO public."asg_v2" ("eno", "pno", "dur") VALUES ('E1', 'P1', 12);
INSERT INTO public."asg_v2" ("eno", "pno", "dur") VALUES ('E2', 'P1', 24);
INSERT INTO public."asg_v2" ("eno", "pno", "dur") VALUES ('E3', 'P3', 10);

-- Table: public."chinhanh"
DELETE FROM public."chinhanh";
INSERT INTO public."chinhanh" ("macn", "tencn", "thanhpho") VALUES ('CN3', 'Điện lực Thủ Đức', 'TP Hồ Chí Minh');

-- Table: public."hoadon"
DELETE FROM public."hoadon";
INSERT INTO public."hoadon" ("sohdn", "thang", "nam", "sohd", "manv", "sotien") VALUES ('BILL301', 1, 2026, 'HD301', 'NV301', 6000000);
INSERT INTO public."hoadon" ("sohdn", "thang", "nam", "sohd", "manv", "sotien") VALUES ('HDN_003_2026_03', 3, 2026, 'HD_003', 'NV03', 1250000);
INSERT INTO public."hoadon" ("sohdn", "thang", "nam", "sohd", "manv", "sotien") VALUES ('HDN_003_02_2026_03', 3, 2026, 'HD_003_02', 'NV0302', 980000);
INSERT INTO public."hoadon" ("sohdn", "thang", "nam", "sohd", "manv", "sotien") VALUES ('HDN_003_03_2026_03', 3, 2026, 'HD_003_03', 'NV03', 1600000);

-- Table: public."hopdong"
DELETE FROM public."hopdong";
INSERT INTO public."hopdong" ("sohd", "ngayky", "makh", "sodienke", "kwdinhmuc", "dongiakw", "ispaid") VALUES ('HD301', '2026-04-07 17:00:00.000+00', 'KH301', '3001', 1500, 4000, FALSE);
INSERT INTO public."hopdong" ("sohd", "ngayky", "makh", "sodienke", "kwdinhmuc", "dongiakw", "ispaid") VALUES ('HD302', '2026-04-07 17:00:00.000+00', 'KH302', '3002', 5000, 4200, FALSE);
INSERT INTO public."hopdong" ("sohd", "ngayky", "makh", "sodienke", "kwdinhmuc", "dongiakw", "ispaid") VALUES ('HD_003', '2026-02-28 17:00:00.000+00', 'KH03', 'DK-CN3-0001', 400, 3500, FALSE);
INSERT INTO public."hopdong" ("sohd", "ngayky", "makh", "sodienke", "kwdinhmuc", "dongiakw", "ispaid") VALUES ('HD_003_02', '2026-03-04 17:00:00.000+00', 'KH0302', 'DK-CN3-0002', 300, 3500, TRUE);
INSERT INTO public."hopdong" ("sohd", "ngayky", "makh", "sodienke", "kwdinhmuc", "dongiakw", "ispaid") VALUES ('HD_003_03', '2026-03-09 17:00:00.000+00', 'KH0303', 'DK-CN3-0003', 500, 3500, FALSE);

-- Table: public."khachhang"
DELETE FROM public."khachhang";
INSERT INTO public."khachhang" ("makh", "tenkh", "macn") VALUES ('KH301', 'Công ty Sài Gòn Mới', 'CN3');
INSERT INTO public."khachhang" ("makh", "tenkh", "macn") VALUES ('KH302', 'Khu Công nghiệp Tân Bình', 'CN3');
INSERT INTO public."khachhang" ("makh", "tenkh", "macn") VALUES ('KH03', 'Lê Thị C', 'CN3');
INSERT INTO public."khachhang" ("makh", "tenkh", "macn") VALUES ('KH0302', 'Phạm Văn D', 'CN3');
INSERT INTO public."khachhang" ("makh", "tenkh", "macn") VALUES ('KH0303', 'Nguyễn Thị E', 'CN3');

-- Table: public."nhanvien"
DELETE FROM public."nhanvien";
INSERT INTO public."nhanvien" ("manv", "hoten", "macn", "password", "role") VALUES ('admin', 'Quản trị viên hệ thống', 'CN3', 'admin', 'admin');
INSERT INTO public."nhanvien" ("manv", "hoten", "macn", "password", "role") VALUES ('admin3', 'Người quản trị - TP HCM', 'CN3', 'admin', 'admin');
INSERT INTO public."nhanvien" ("manv", "hoten", "macn", "password", "role") VALUES ('NV301', 'Trần Thị Hương', 'CN3', '123', 'user');
INSERT INTO public."nhanvien" ("manv", "hoten", "macn", "password", "role") VALUES ('NV302', 'Đặng Văn Kiên', 'CN3', '123', 'user');
INSERT INTO public."nhanvien" ("manv", "hoten", "macn", "password", "role") VALUES ('NV303', 'Nguyễn Thị Lam', 'CN3', '123', 'user');
INSERT INTO public."nhanvien" ("manv", "hoten", "macn", "password", "role") VALUES ('NV03', 'Nguyễn Tấn Kiệt', 'CN3', NULL, NULL);
INSERT INTO public."nhanvien" ("manv", "hoten", "macn", "password", "role") VALUES ('NV0302', 'Trần Thị Bích', 'CN3', NULL, NULL);

-- Table: public."orders2"
DELETE FROM public."orders2";
INSERT INTO public."orders2" ("custid", "prodid", "orderdate") VALUES ('C01', 'SP01', '2026-03-20');
INSERT INTO public."orders2" ("custid", "prodid", "orderdate") VALUES ('C01', 'SP02', '2026-03-21');
INSERT INTO public."orders2" ("custid", "prodid", "orderdate") VALUES ('C02', 'SP01', '2026-03-19');
INSERT INTO public."orders2" ("custid", "prodid", "orderdate") VALUES ('C03', 'SP03', '2026-03-18');

-- Table: public."orders2hoz"
DELETE FROM public."orders2hoz";
INSERT INTO public."orders2hoz" ("custid", "prodid", "qty", "orderdate") VALUES ('C02', 'SP01', 5, '2026-03-19');

-- Table: public."users"
DELETE FROM public."users";
INSERT INTO public."users" ("manv", "email", "password", "role", "salt") VALUES ('NV03', 'tankiet@huflit.edu.vn', 'hash_pass_3', 'admin', 'salt_3');
INSERT INTO public."users" ("manv", "email", "password", "role", "salt") VALUES ('NV0302', 'bichtran@huflit.edu.vn', 'hash_pass_0302', 'staff', 'salt_0302');

COMMIT;
