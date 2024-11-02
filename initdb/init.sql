CREATE USER keycloak WITH PASSWORD 'keycloak';
CREATE DATABASE keycloak;
GRANT ALL PRIVILEGES ON DATABASE keycloak TO keycloak;
GRANT USAGE ON SCHEMA public TO keycloak;
GRANT ALL ON DATABASE keycloak TO keycloak;
ALTER DATABASE keycloak OWNER TO keycloak;