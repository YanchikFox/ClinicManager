# Developer Guide

## Database connection management

The application relies on a single JDBC `Connection` that is owned by `RepositoryManager`.
The manager opens the connection through `DriverManager` and passes the handle to each
repository. This guarantees that every data access object operates on the same
transactional context and avoids SQLite `database locked` exceptions caused by competing
connections.

* Repositories never call `DriverManager` directly – they receive the ready connection
  via their constructors.
* `RepositoryManager` enables foreign keys once on the shared connection and can be used
  in a try-with-resources block (`RepositoryManager implements AutoCloseable`).
* Call `RepositoryManager#connection()` when you need direct access to the underlying
  JDBC handle (for example, to manage transactions or PRAGMA settings). Avoid closing it
  manually if the manager created the connection.
* Close the manager (or call `closeAll()`) when the application shuts down to dispose of
  prepared statements and, if owned, the underlying connection.

## Working with transactions

SQLite defaults to auto-commit. To perform operations that must succeed or fail
atomically across several repositories:

1. Obtain the connection from the `RepositoryManager` via `repositoryManager.connection()`
   (or keep a reference to the connection you supplied when constructing it).
2. Disable auto-commit: `connection.setAutoCommit(false);`
3. Execute repository calls (`patients().save(...)`, `appointments().save(...)`, etc.).
   They will all use the shared connection.
4. Finish with `connection.commit()` or `connection.rollback()` in a `finally` block.
5. Restore auto-commit if needed.

The integration test `RepositoryTransactionIntegrationTest` demonstrates how to orchestrate
multiple repositories within a single transaction against an in-memory SQLite database.
