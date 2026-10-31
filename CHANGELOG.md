# Changelog

## 1.2.0 (unreleased)

### Added
- `FileUtils.readMatrixRows` and `MatrixRowHandler`: read a matrix one row at a time, without collecting it. The
  header checks (duplicate columns) and the entries-per-row check are the same as `readMatrix`, which now uses it.
- `KeyValuePair`, an immutable record of a key and a value (either may be null), and `KeyValueLineProcessor`, which
  maps a matrix row to `KeyValuePair`s with conversion functions (e.g. `Function.identity()`, `Double::valueOf`)
  instead of reflection on `String` constructors. They replace `KeyValue` and `SimpleLineProcessor`.

### Security
- `CompressUtils.unzip` wrote an entry wherever its name pointed ("zip slip"): an entry named `../x` was written
  outside the output folder. Such an entry is now rejected with an `IOException`, and nothing is written for it.

### Fixed
- `CompressUtils.unzip` wrote a directory entry as an empty file, so the files inside it could not be written; it now
  creates the directory. The returned list holds the files written (no longer the directory entries), and a missing
  output folder is created with its parents.
- `CompressUtils.zipDirectory` failed on a sub-directory (it now includes it, recursively), threw a
  `NullPointerException` for a missing directory (now an `IOException`), and named entries with the platform's
  separator (`\` on Windows, invalid in a zip); entries now always use `/`, in sorted order.
- `MathUtils.accumulate` failed on an empty array; it now gives an empty array.
- `FileUtils.readMatrixRows` and `readMatrix` threw a `NullPointerException` for an empty file; they now throw an
  `IOException` saying the file has no header line.
- The `FileUtils` readers (`skip`, `readKeyValue`, `readKeyDoubleValue`, `readKeyValues`, `readCSV`, and the deprecated
  `readKeyValuesEngin` and `readContentAsString`) tested for the end of a file with `BufferedReader.ready()`, which
  only says whether a read would block: a reader that is not ready yet (e.g. a pipe) was cut short. They now read
  until `readLine()` returns null.
- `SimpleLineProcessor` printed the stack trace of a value it could not read and returned the row without it. It now
  throws the value class's own exception (e.g. `NumberFormatException`), or `IllegalArgumentException` for a class
  without a public `String` constructor.

### Deprecated (to be removed in 2.0.0)
- The constants nothing uses: `Constants.BACK_SLASH`, `DOT`, `HASH`, `ID`, `NULL`, `SLASH`, `SPACE`.
- `KeyValue` → `KeyValuePair`. `KeyValue.equals` is true when the keys **or** the values are equal, which does not
  match its `hashCode`, and `hashCode`/`toString` fail on a null value; it is left as it is until removal.
- `SimpleLineProcessor` → `KeyValueLineProcessor`: same parsing, but conversion functions instead of classes
  (`SimpleLineProcessor` calls `String` constructors by reflection, e.g. `new Double(String)`, which the JDK deprecates
  for removal).
- `FileUtils.copy` → `java.nio.file.Files.copy`; `getIOTmpDir` → `System.getProperty("java.io.tmpdir")`;
  `readContentAsString` → `Files.readString` (which keeps the line separators); `getFileHeader` → read the first line;
  `deleteQuietly` → `Files.deleteIfExists`; `readKeyValuesEngin` → `readCSV(File, int, Function)`;
  `readCSV(File, String, int, Function)` → `readCSV(File, String, int)` or `readCSV(File, int, Function)`.

### Changed
- `CompressUtils` uses `java.nio.file` streams and `transferTo` instead of copy loops over byte buffers, and is `final`
  (it only had a private constructor). `Constants.NEW_LINE` is `System.lineSeparator()` (the same value).
- Javadoc builds without errors (`gradle javadoc`); `MathUtils.computeRatio` is documented with a valid table.
- `FileUtils.writeObjects` writes through a buffer (same output, fewer writes).
- Lists returned by `readMatrix`, `readKeyValues` and `readCSV` are `ArrayList`s (they are declared as `List`).
- Javadoc rewritten where it was missing or stale (e.g. references to `LineIterator`); author address omixer.io.
- Build: tests on JUnit 6.1.3 (Jupiter) instead of JUnit 4 through Spock 1.1 / Groovy 2.4; the published version is the
  project version.

### Kept for 2.0.0 (would break the API now)
- `MatrixLineProcessor` with a single `process(FileLine, String)`; `Constants.ZERO` as a primitive `double`; a private
  constructor for `MathUtils`; removal of everything deprecated above.
