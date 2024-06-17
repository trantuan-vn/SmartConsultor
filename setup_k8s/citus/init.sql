-- master
psql -U postgres
CREATE USER smartconsultor WITH PASSWORD 'secret99';
-- master, all worker
psql -U postgres
CREATE DATABASE smartconsultor;
-- master
psql -U postgres
GRANT CREATE ON DATABASE smartconsultor TO smartconsultor;
psql -U smartconsultor -d smartconsultor
CREATE SCHEMA standing;
CREATE SCHEMA history;

-- master, all worker
psql -U postgres -d smartconsultor
CREATE EXTENSION citus;

--master 
psql -U postgres -d smartconsultor
SELECT citus_set_coordinator_host('citus-master-0', 5432);
SELECT * from citus_add_node('citus-worker-0.citus-workers', 5432);
SELECT * from citus_add_node('citus-worker-1.citus-workers', 5432);
SELECT * FROM citus_get_active_worker_nodes();
ALTER SYSTEM SET citus.shard_replication_factor TO 2;
SELECT pg_reload_conf();

--master
psql -U smartconsultor -d smartconsultor
CREATE TABLE standing.orders (
    order_id bigserial PRIMARY KEY,
    customer_id bigint,
    order_date date,
    amount numeric
);
SELECT create_distributed_table('standing.orders', 'order_id');

