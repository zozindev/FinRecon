insert into app_users (username, password_hash, user_role, active, created_at)
values
    ('admin', '5c06eb3d5a05a19f49476d694ca81a36344660e9d5b98e3d6a6630f31c2422e7', 'ADMIN', true, current_timestamp),
    ('operator', '1507a31bede6c1e3e972e0ace00f4e928925331c16fd635ab88f694157869ddc', 'OPERATOR', true, current_timestamp);
