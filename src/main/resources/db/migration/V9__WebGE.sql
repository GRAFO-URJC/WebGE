ALTER TABLE webge.user DROP COLUMN file_upload_id;
drop table webge.files_upload;
ALTER TABLE webge.grammar DROP COLUMN run_id;
ALTER TABLE webge.experiment_data_type DROP COLUMN run_id;
ALTER TABLE webge.experiment_data_type RENAME TO dataset;