drop table if exists webge.expdatatype_list;
drop table if exists webge.exp_data_type_list;
drop table if exists webge.grammar_list;

create table webge.exp_data_type_list
(
    experiment_id           bigint null,
    experimentdatatype_id bigint not null,
    constraint FKnaoei6ntci1avr8817b0erkj4
        foreign key (experiment_id) references webge.experiment (experiment_id),
    constraint FKdnqwdjhocp54iixxons1mjf5r
        foreign key (experimentdatatype_id) references webge.experiment_data_type (experimentdatatype_id),
    primary key (experiment_id,experimentdatatype_id)
);

create table webge.grammar_list
(
    experiment_id bigint null,
    grammar_id    bigint not null,
    constraint FKgkwvx8qepkv60fnfgaa43ocu9
        foreign key (experiment_id) references webge.experiment (experiment_id),
    constraint FKrflr6gyogc45e7c7omrr9qs6b
        foreign key (grammar_id) references webge.grammar (grammar_id),

    primary key(experiment_id,grammar_id)
);

