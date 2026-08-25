-- Seed real book covers provided by user
UPDATE novels SET cover_url = NULL WHERE slug IN ('tat-den', 'chua-tau-kim-quy');
UPDATE novels SET cover_url = '/covers/con-nha-giau.png' WHERE slug = 'con-nha-giau';
UPDATE novels SET cover_url = '/covers/leu-chong.png' WHERE slug IN ('leu-chong', 'leu-chong-muc-luc');
UPDATE novels SET cover_url = '/covers/vo-quyt-day-mong-tay-nhon.png' WHERE slug = 'vo-quyt-day-mong-tay-nhon';
UPDATE novels SET cover_url = '/covers/tu-hon.png' WHERE slug = 'tu-hon';
UPDATE novels SET cover_url = '/covers/tro-vo-lua-ra.png' WHERE slug = 'tro-vo-lua-ra';
UPDATE novels SET cover_url = '/covers/thay-chung-trung-so.png' WHERE slug = 'thay-chung-trung-so';
UPDATE novels SET cover_url = '/covers/tien-bac-bac-tien.png' WHERE slug = 'tien-bac-bac-tien';
UPDATE novels SET cover_url = '/covers/tai-mang-tuong-do.png' WHERE slug = 'tai-mang-tuong-do';
UPDATE novels SET cover_url = '/covers/phong-tran-tham-su.png' WHERE slug = 'phong-tran-tham-su';
