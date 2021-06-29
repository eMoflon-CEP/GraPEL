# GraPEL
This repository contains GrapeL, a textual language to specify and generate integrated solutions combining both Incremental Graph Pattern Matching  (IGPM) and Complex Event Processing (CEP).

As such, GrapeL is implemented in eMoflon, which is a state-of-the-art graph transformation tool that employs IGPM techniques to search for patterns in models, which are graph-like data structures.

Using eMoflon, we can specify and find matches to our patterns in a given model incrementally, i.e., new matches but also invalidated matches are detected and reported.

However, it is often hard to analyze matches w.r.t. to temporal dependencies, e.g., relating the order in which they were detected.

For this purpose, we take these matches and convert them to events that can be fed to a CEP engine to detect exactly those relationships.

To this point, we incorporate Apama as our CEP engine of choice, which is a industrial strength tool developed by SoftwareAG.

# Installation
The GraPEL install process needs API jars provided by Apama.
Due to license and legal requirements, these jars cannot be directly provided.
Thus, the installation is a bit more complicated.
This part will provide a guide on how to install GraPEL in Eclipse.

1. Install Apama (or Apama Community Edtion (NOT TESTED)) (tbd)
2. Download and install Eclipse (Modelling) (tested with eclipse 2021-03)
3. Follow the [install instructions for eMoflon](https://github.com/eMoflon/emoflon-ibex-updatesite#how-to-install)
4. Install MWE category (MWE SDK and MWE2 Language SDK) and the XText category from the [XText updatesite](http://download.eclipse.org/modeling/tmf/xtext/updates/composite/releases/)
5. Import the the projects of the GraPEL, [grapel-deployment](https://github.com/eMoflon-CEP/grapel-deployment) and [ApamaDependencies](https://github.com/eMoflon-CEP/ApamaDependencies) repositories into eclipse (TBD)
6. Locate the Apama API jars contained in the Apama installation.
For the Community Edition the needed .jars can be found in the installation zip:
apama_10.5.3.2_amd64_linux.zip\apama_10.5.3.2_amd64_linux\data\SAGImage.zip\e2ei,11,PAM_10.5.3.2.377593,BM_PAM_SagCommonLibJava-ALL-Any.zip\Apama\lib
7. Extract and copy ap-client.jar, ap-correlator-extenstion-api.jar and ap-util.jar into the ApamaDependencies Project
8. Generate the grapelmodel code by opening the GrapeLModel.genmodel file in grapelmodel/model and right clicking the GrapeLModel. Select "Generate Model Code"
9. Run the MWE build by right clicking the GenerateGrapel.mwe2 in the org.emoflon.cep package within grapel/src and selecting "Run as -> MWE2 Workflow"
10. Open the site.xml in the grapel.updatesite project under stable/updatesite/site.xml
11. Build the update site by selecting the grapel.feature feature in the grapel.category and clicking build
10. Open the eclipse install wizard by clicking on "Help -> Install new Software"
11. Select the newly build update site in the "Work with" field of the wizard. You may need to restart eclipse to see the new local update site.
12. Select GraPEL and go through the wizard to finish the GraPEL installation.

You now have installed GraPEL in Eclipse.
