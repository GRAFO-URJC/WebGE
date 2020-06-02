alter table webge.run
    drop column execution_report;

create table webge.run_execution_report
(
    run_id  bigserial primary key,
    execution_report text null,
    foreign key (run_id) references webge.run (run_id)
);