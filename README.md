# nsql
Execute Netsuite's SuiteQL from the command line

Depends on:
- com.netsuite.nsql SuiteApp installed on your Netsuite account.
- Babashka (https://github.com/borkdude/babashka)
- jq (https://github.com/stedolan/jq)

Usage:
`$ nsql "select accountsearchdisplayname from account"`
