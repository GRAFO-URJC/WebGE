ALTER TABLE webge.run DROP COLUMN default_grammar_id;
ALTER TABLE webge.experiment DROP COLUMN default_grammar;
alter table webge.experiment
    add grammar text null;
drop table webge.grammar_list;
