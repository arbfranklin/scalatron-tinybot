## tinybot

[![tinybot 1.8 bot-war](https://img.youtube.com/vi/_oQcLmPfut0/0.jpg)](https://www.youtube.com/watch?v=\_oQcLmPfut0)

This is the home of "tinybot", an implementation of a [Scalatron](http://scalatron.github.com) bot. On _19 July 2012_, tinybot was validated to have a high score of 12,330,400 on the [freestyle benchmark](http://scalatron.github.com/pages/benchmark.html). The latest version scores ~18 million.

The general approach of tinybot is as follows:
* A set of strategies are used to vote on the master (& slaves) next move through a collaborative process.
* Strategies can abstain from voting or mandate a particular move in certain circumstances.
* All strategies and associated moves are weighted using a "Genome" for the run.
* tinybot can self tune its "Genome" by running using a genetic algorithm style approach.
