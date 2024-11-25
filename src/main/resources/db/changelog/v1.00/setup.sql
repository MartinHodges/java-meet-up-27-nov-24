drop table if exists fish_tanks cascade;
drop table if exists fish_tanks_fishes cascade;
drop table if exists fishes cascade ;
create table fish_tanks (id uuid not null, name varchar(255), primary key (id));
create table fish_tanks_fishes (fish_tank_entity_id uuid not null, fishes_id uuid not null unique, primary key (fish_tank_entity_id, fishes_id));
create table fishes (fish_tank_id uuid, id uuid not null, type varchar(255), primary key (id));
alter table if exists fish_tanks_fishes add constraint FKc5ufdsl9yn831nwdn95ojx1wn foreign key (fishes_id) references fishes;
alter table if exists fish_tanks_fishes add constraint FKjfo9itnyt1dxv2p3ninuibqlk foreign key (fish_tank_entity_id) references fish_tanks;
alter table if exists fishes add constraint FKhc04bdx0vvcvy4ppnve9eocny foreign key (fish_tank_id) references fish_tanks;
