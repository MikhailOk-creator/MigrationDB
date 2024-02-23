create table if not exists band_t (
	id int4 NOT NULL GENERATED ALWAYS AS identity primary key,
	name_of_band varchar NOT NULL,
	start_year int4 NOT NULL,
	end_year int4 NULL
);

create table if not exists musician_t (
	id int NOT NULL GENERATED ALWAYS AS identity primary key,
	name_of_musician varchar NOT NULL,
	country varchar NOT NULL,
	full_real_name varchar NULL,
	birth_year int NOT NULL,
	death_year varchar NULL
);

CREATE TABLE band_and_musician_t (
	band_id int NOT NULL,
	musician_id int NOT NULL,
	musical_instrument varchar NOT null,
	primary key (band_id, musician_id),
	foreign key (band_id) references band_t(id),
	foreign key (musician_id) references musician_t(id)
);