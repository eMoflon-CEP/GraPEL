# GraPEL
This repository contains GrapeL, a textual language to specify and generate integrated solutions combining both Incremental Graph Pattern Matching  (IGPM) and Complex Event Processing (CEP).

As such, GrapeL is implemented in eMoflon, which is a state-of-the-art graph transformation tool that employs IGPM techniques to search for patterns in models, which are graph-like data structures.

Using eMoflon, we can specify and find matches to our patterns in a given model incrementally, i.e., new matches but also invalidated matches are detected and reported.

However, it is often hard to analyze matches w.r.t. to temporal dependencies, e.g., relating the order in which they were detected.

For this purpose, we take these matches and convert them to events that can be fed to a CEP engine to detect exactly those relationships.

To this point, we incorporate Apama as our CEP engine of choice, which is a industrial strength tool developed by SoftwareAG.

# Installation:
