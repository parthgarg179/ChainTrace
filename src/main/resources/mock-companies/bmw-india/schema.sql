-- =====================================================================
-- MOCK COMPANY DATABASE: BMW India (cars)
-- Invented data for a college project, not BMW's real records.
-- ChainTrace reads it only through configuration (companies/bmw-india.yml),
-- not through company-specific Java code.
-- =====================================================================

-- Re-runnable: drop in reverse dependency order first.
DROP TABLE IF EXISTS title_transfers;
DROP TABLE IF EXISTS vehicles;
DROP TABLE IF EXISTS account_holders;

CREATE TABLE account_holders (
    acc_no           VARCHAR(12)   PRIMARY KEY,
    first_name       VARCHAR(60)   NOT NULL,
    last_name        VARCHAR(60)   NOT NULL,
    login_email      VARCHAR(150)  NOT NULL UNIQUE,
    password_sha256  CHAR(64)      NOT NULL,
    state_code       CHAR(2)
);

CREATE TABLE vehicles (
    vin                     CHAR(17)      PRIMARY KEY,
    make_model              VARCHAR(100)  NOT NULL,
    variant                 VARCHAR(60),
    fuel_type               VARCHAR(20),
    manufactured_on         DATE,
    warranty_valid_till     DATE,
    first_registered_state  VARCHAR(40),
    accident_reported       BOOLEAN       DEFAULT FALSE NOT NULL,
    open_recall             VARCHAR(120)  -- name of an open recall campaign, NULL if none
);

-- Transfer codes: DEALER_SALE (first sale from a dealer) or RESALE.
CREATE TABLE title_transfers (
    transfer_id    INT           PRIMARY KEY,
    vin            CHAR(17)      NOT NULL,
    to_acc_no      VARCHAR(12)   NOT NULL,
    transfer_date  DATE          NOT NULL,
    transfer_mode  VARCHAR(20)   NOT NULL,
    odometer_km    INT,
    dealer_name    VARCHAR(100),
    CONSTRAINT fk_tt_vehicle FOREIGN KEY (vin)       REFERENCES vehicles(vin),
    CONSTRAINT fk_tt_holder  FOREIGN KEY (to_acc_no) REFERENCES account_holders(acc_no)
);
