-- Gán quyền SELECT và INSERT cho người dùng app trên tất cả các bảng trong cơ sở dữ liệu app
GRANT SELECT, INSERT ON ALL TABLES IN SCHEMA public TO app;

-- Gán quyền USAGE cho người dùng app trên tất cả các sequence trong cơ sở dữ liệu app
GRANT USAGE ON ALL SEQUENCES IN SCHEMA public TO app;

-- Gán quyền SELECT và USAGE cho người dùng app trên tất cả các sequences trước đó
GRANT SELECT, USAGE ON ALL SEQUENCES IN SCHEMA public TO app;

-- Gán quyền EXECUTE cho người dùng app trên tất cả các functions trong cơ sở dữ liệu app
GRANT EXECUTE ON ALL FUNCTIONS IN SCHEMA public TO app;
