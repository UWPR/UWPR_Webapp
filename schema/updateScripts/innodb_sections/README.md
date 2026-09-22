# InnoDB migration

Converts `mainDb` and `pr` from MyISAM to InnoDB and adds 36 foreign keys.

## Before starting

```sql
SELECT COUNT(*) FROM mainDb.instrumentUsage WHERE updatedBy IS NULL OR updatedBy = 0;   -- 0
```

If that returns anything other than 0, run `recover_updatedBy.sql` from the parent directory, ahead of
everything in the table below. Section 2 makes `instrumentUsage.updatedBy` NOT NULL and section 5 keys
it to `tblResearchers`, so every block must name a real researcher. Section 2 stops with `ERROR 1265`
on a surviving NULL, three scripts in.

Stop the application before taking the dump below. Leave it stopped until every script here has run
and the new WAR is in place. A build without `DBConnectionManager.endTransactionAndClose` restores
autocommit on a connection holding an open transaction, which commits it. On MyISAM the writes had
already landed. On InnoDB it commits half a transaction.

Take a dump of both schemas. Section 1 deletes rows from MyISAM tables, where `ROLLBACK` is accepted
and ignored.

```bash
mysqldump -u <user> -p --databases mainDb pr --skip-triggers \
  --result-file="$HOME/backups/mainDb_pr-pre-innodb-<date>.sql"
```

Section 5 adds keys from `pr` into `mainDb`, so restore both together or neither.

The connection needs `DELETE` and `ALTER` on both schemas. Several statements join across them.

## Running

Name `mainDb` as the default database. Section 1 stops at its first statement with "No database
selected" otherwise.

```bash
mysql -u <admin> -p mainDb -t -vvv < section_1.sql | tee section_1.log
```

`-vvv` prints each statement, its row count and its time. `DELETE` and `ALTER` return no rows, so
section 1 and section 3 log nothing without it. `-t` prints results as tables.

Run the scripts in this order. Read each one's output before starting the next.

| | Script | Does | Verify |
|---|---|---|---|
| 1 | `section_0_census.sql` | Counts the rows section 1 deletes | Read-only |
| 2 | `section_1.sql` | Deletes orphaned rows | The closing 37 counts read 0 |
| 3 | `section_2.sql` | Widens the id columns, makes `updatedBy` NOT NULL | 12 columns read `int(10) unsigned` |
| 4 | `section_0_rowcount.sql` | Counts every row in both schemas | Read-only |
| 5 | `section_3.sql` | Converts 35 tables to InnoDB | An empty result |
| 6 | `section_0_rowcount.sql` | Counts them again | Matches step 4, table by table |
| 7 | `section_4.sql` | Adds the 18 indexes the keys need | `indexes_added` reads 18 |
| 8 | `section_5.sql` | Adds 36 foreign keys | One row per `ADD CONSTRAINT` |

Section 2 also prints three `AUTO_INCREMENT` values. Each must be unchanged and above its `MAX`. Read
them before the section runs, since it reports them either way.

Section 3 searches for tables still on MyISAM, so an empty result is the pass.

Stop on a failure and do not run the next script. Sections 1 to 4 delete children before parents and
are safe to leave half run. Section 5 adds keys one at a time, so a failure there leaves some added
and some not.

## The two section_0 scripts

Both are read-only. Neither is a section.

`section_0_census.sql` is section 1's verification query without the deletes. Section 1 deletes around
2900 rows, reports no count for them under a plain invocation, and closes with a census that reads 0.
Running the census first records the state before section 1.

`section_0_rowcount.sql` counts every row in both schemas with `COUNT(*)`. Run it before and after
section 3 and compare the output. `information_schema.table_rows` is exact on MyISAM and sampled on
InnoDB, so once the conversion is done there is no exact before left to recover. A table that loses
rows in the rebuild appears in no other output.
