DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM webge.user GROUP BY username HAVING COUNT(*) > 1) THEN
        RAISE EXCEPTION 'Duplicate usernames must be resolved before applying V26';
    END IF;
    IF EXISTS (SELECT 1 FROM webge.user GROUP BY email HAVING COUNT(*) > 1) THEN
        RAISE EXCEPTION 'Duplicate email addresses must be resolved before applying V26';
    END IF;
END
$$;

CREATE UNIQUE INDEX uk_webge_user_username ON webge.user(username);
CREATE UNIQUE INDEX uk_webge_user_email ON webge.user(email);
