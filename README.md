This is the home of "tinybot", an implementation of a [Scalatron](http://scalatron.github.com) bot.

The general approach of tinybot is as follows:
* A set of strategies are used to vote on the master (& slaves) next move through a collaborative process.
* Strategies can abstain from voting or mandate a particular move in certain circumstances.
* All strategies and associated moves are weighted using a "Genome" for the run.
* tinybot can self tune its "Genome" by running using a genetic algorithm style approach.

This is my first Scala project, so any code improvements/suggestions would be helpful!

## News
* *19 July 2012* - 12.3 million, validated score of tinybot 1.0 on the [freestyle benchmark](http://scalatron.github.com/pages/benchmark.html).

## License

tinybot is licensed under the Simplified BSD License.