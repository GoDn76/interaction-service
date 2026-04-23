INSERT INTO users (id, username, is_premium)
VALUES (
           '11111111-1111-1111-1111-111111111111',
           'test_human',
           false
       )
ON CONFLICT (id) DO NOTHING;

INSERT INTO bots (id, name, persona_description)
VALUES (
           '22222222-2222-2222-2222-222222222222',
           'test_bot',
           'Very Cool Test Bot Entire DB is frozen'
       )
ON CONFLICT (id) DO NOTHING;