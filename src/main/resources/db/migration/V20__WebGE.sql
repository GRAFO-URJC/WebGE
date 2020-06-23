ALTER TABLE webge.experiment DROP COLUMN crossexperiment;
alter table webge.dataset
    add fold_size int;