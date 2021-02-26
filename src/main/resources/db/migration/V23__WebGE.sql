-- Indexing elements:
create index exp_userId on webge.experiment(user_id);
create index run_expId on webge.run(experiment_id_experiment_id);