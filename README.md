# csv-parser

## Run

To run all test:
```sbtshell
clean test
```

To start application use:
```sbtshell
run
```

### What is implemented?
- [x] The quoting character should be configurable, defaulting to "
- [x] The line separator should also be configurable, defaulting to \n
- [x] The field delimiter should also be configurable, defaulting to ,
- [ ] The CSV can contain an optional header row
- [x] String values can be optionally quoted, enclosed in the predefined quoting character
- [x] The parser should handle new line characters embedded in a quoted cell
- [x] The parser should handle field delimiters inside quoted cells
- [x] Two consecutive field delimiters mean the value is absent/null
- [x] A field where part of the text appears as "quoted" and part as not quoted should be interpreted as a single field
- [ ] The input file can potentially be very large
- [ ] Handle files in arbitrary encodings other than UTF-8