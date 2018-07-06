CREATE TABLE schedule_job_log (
  log_id bigserial,
  job_id int8 NOT NULL,
  bean_name varchar(200),
  method_name varchar(100),
  params varchar(2000),
  status int NOT NULL,
  error varchar(2000),
  times int NOT NULL,
  create_time timestamp,
  PRIMARY KEY (log_id)
);