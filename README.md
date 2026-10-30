# omixer-utils

Small, generic Java helpers for reading and writing text files, used by
[Omixer-RPM](https://github.com/raeslab/omixer-rpm). They read and pass on lines; they never interpret them.

- `FileUtils`: delimited key-value files and rows, objects written one per line, and matrices with a header line —
  collected (`readMatrix`) or streamed one row at a time to a `MatrixRowHandler` (`readMatrixRows`), with each row's
  file name and line number (`FileLine`) for error messages.
- `CompressUtils`: gzip, gunzip, zip and unzip.
- `MathUtils`: fold-change ratios and cumulative sums.

## Build

Java 17 or newer; Gradle 9.

```
gradle build                 # compile and test
gradle publishToMavenLocal   # install io.omixer:omixer-utils into ~/.m2, where Omixer-RPM resolves it
```

Changes are listed in [CHANGELOG.md](CHANGELOG.md).

## License

MIT, see [LICENSE](LICENSE).
