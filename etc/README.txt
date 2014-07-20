This directory contains scripts and the raw data from which the entries
were assembled.

I first pulled Tom Merritt's tech-history citation document at
https://docs.google.com/document/pub?id=1Rvh4vtiP1bIPGtbm_r37xR-IrL_ZAhgflqsniEGonqQ
into citations.html

I then extracted the text of the entries along with with some minor
character encoding cleanup with Clean.java into citations.txt

This was followed by some hand-edits to citations.txt to clean up a
few inconsistent uses of the date field.

Finally, gen.pl converts citations.txt into json files split up by
day, which moved into assets to be read by the app at runtime.
