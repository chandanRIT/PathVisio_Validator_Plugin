PathVisio Validator Plugin
-------

The PathVisio Validator plugin aims to simplify the task of producing biological pathway diagrams using standardized graphical notations, such as Molecular Interaction Maps or the Systems Biology Graphical Notation. This plugin assists in the creation of pathway diagrams by ensuring correct usage of a notation thereby reducing ambiguity when diagrams are shared amongst biologists. Rulesets needed in the validation process, can be generated for any graphical notation that a user desires, using either Schematron or Groovy. The plugin also provides support for filtering validation results, validating on a subset of rules, and distinguishing errors and warnings. 

HOW TO BUILD
------------

You need to have installed:

* Java
* Ant

Simply type "ant" from the source directory.
This will result in the creation of ??DIR??/ValidatorPlugin.jar, 
which you can be added to the USER_HOME/.Pathvisio/plugins directory.

FILES
-----

Here is an explanation of the directories in this project:

images        - images used in the plugin
lib           - Java libraries used with the plugin
rulesets      - validation rulesets included with the plugin 
source        - validator plugin source code 
xsls          - Schematron stylesheets for converting 
GroovySupport - ???

AUTHORS
-------

Kumar Chandan
Augustin Luna
Martijn van Iersel

CONTACT
-------

Email: 
augustin@mail.nih.gov or chandankmit@gmail.com

Our official website:
http://pathvisio.org/wiki/PathwayValidatorHelp
