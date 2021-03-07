-- Remove table with run report and use field in run.
alter table webge.run add exec_report text default '';
update webge.run r set exec_report=(select execution_report from webge.run_execution_report rr where rr.run_id=r.run_id);
drop table webge.run_execution_report;

