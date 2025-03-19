Contributing to langchain4j-microprofile-jakarta
===================================================

Welcome to the smallrye-llm project! We welcome contributions from the community. This guide will walk you through the steps for getting started on our project.

- [Contributing Guidelines](#contributing-guidelines)
- [Issues](#issues)
  - [Good First Issues](#good-first-issues)
- [Setting up your Developer Environment](#setting-up-your-developer-environment)
- [Community](#community)

## Contributing Guidelines

Please refer to our Wiki for the [Contribution Guidelines](https://github.com/langchain4j/langchain4j-microprofile-jakarta).


## Issues
The langchain4j-microprofile-jakarta project uses GitHub to manage issues. All issues can be found [here](https://github.com/langchain4j/langchain4j-microprofile-jakarta/issues). 

To create a new issue, comment on an existing issue, or assign an issue to yourself, you'll need to first [create a GitHub account](https://github.com/).


### Good First Issues
Want to contribute to the langchain4j-microprofile-jakarta project but aren't quite sure where to start? Check out our issues with the `good first issue` label. These are a triaged set of issues that are great for getting started on our project. These can be found [here](https://github.com/langchain4j/langchain4j-microprofile-jakarta/labels/good%20first%20issue). 

Once you have selected an issue you'd like to work on, make sure it's not already assigned to someone else, and assign it to yourself.

It is recommended that you use a separate branch for every issue you work on. To keep things straightforward and memorable, you can name each branch using the GitHub issue number. This way, you can have multiple PRs open for different issues. For example, if you were working on [issue-125](https://github.com/langchain4j/langchain4j-microprofile-jakarta/issues/125), you could use issue-125 as your branch name.

## Setting up your Developer Environment
You will need:

* Python 3.12+
* Git
* An [IDE](https://en.wikipedia.org/wiki/Comparison_of_integrated_development_environments#Java)
(e.g., [Apache NetBeans](https://netbeans.apache.org/))

To setup your development environment you need to:

1. First `cd` to the directory where you cloned the project (eg: `cd langchain4j-microprofile-jakarta`)

2. To build `langchain4j-microprofile-jakarta` run:
    
    mvn clean install


3. To run the tests:   
        

    mvn test
        

## Code Reviews

All submissions, including submissions by project members, need to be reviewed by at least one langchain4j-microprofile-jakarta committer before being merged.

The [GitHub Pull Request Review Process](https://docs.github.com/en/pull-requests/collaborating-with-pull-requests/reviewing-changes-in-pull-requests/about-pull-request-reviews) is followed for every pull request.