CREATE TABLE ATG_CORE.dcspp_google_order
(
  google_order_id  VARCHAR2(40),
  RISK_INFORMATION_RECEIVED  INTEGER,
  order_id            VARCHAR2(40)
)


ALTER TABLE ATG_CORE.dcspp_google_order ADD (
  PRIMARY KEY
 (order_id));

ALTER TABLE ATG_CORE.dcspp_google_order ADD (
  FOREIGN KEY (order_id) 
 REFERENCES ATG_CORE.DCSPP_ORDER (ORDER_ID));

 ALTER TABLE ATG_CORE.DCSPP_GOOGLE_ORDER
 ADD (RISK_INFORMATION_RECEIVED  INTEGER);

 