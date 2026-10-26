-- =============================================================================
-- Migration Seed: V2__seed_users_from_core.sql
-- Description: Seed initial mock data for users from occupify-core
-- =============================================================================

INSERT INTO users (id, email, full_name, avatar_url, created_at)
VALUES
    ('00000000-0000-0000-0000-000000000001', 'dev-employer@occupify.local', 'Tôi (Chủ dự án)', 'https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=150', CURRENT_TIMESTAMP),
    ('11111111-1111-1111-1111-111111111111', 'hoa.le@smartlog.vn', 'Lê Thị Hoa', 'https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=150', CURRENT_TIMESTAMP),
    ('22222222-2222-2222-2222-222222222222', 'dung.pham@insightmetrics.vn', 'Phạm Văn Dũng', 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150', CURRENT_TIMESTAMP),
    ('33333333-3333-3333-3333-333333333333', 'khoa.nguyen@gmail.com', 'Nguyễn Minh Khoa', 'https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150', CURRENT_TIMESTAMP),
    ('44444444-4444-4444-4444-444444444444', 'hung.le@gmail.com', 'Lê Văn Hùng', 'https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=150', CURRENT_TIMESTAMP),
    ('55555555-5555-5555-5555-555555555555', 'thu.nguyen@gmail.com', 'Nguyễn Thị Thu', 'https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=150', CURRENT_TIMESTAMP),
    ('66666666-6666-6666-6666-666666666666', 'duc.anh@gmail.com', 'Phạm Đức Anh', 'https://images.unsplash.com/photo-1519085360753-af0119f7cbe7?w=150', CURRENT_TIMESTAMP)
ON CONFLICT (id) DO UPDATE SET
    email = EXCLUDED.email,
    full_name = EXCLUDED.full_name,
    avatar_url = EXCLUDED.avatar_url;
