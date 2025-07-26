# Casimo

### About the project
A sophisticated casino simulation built with Scala 3 and Scala.js, featuring realistic customer behavior, multiple casino games, and real-time data visualization.

### Live demo
A live demo can be found through the [repo GitHub's page](https://nickghignatti.github.io/casimo/)

### Documentation
All the documentation is accessible at the following link: [Casimo documentation](https://nickghignatti.github.io/casimo/docs)

### Local dev
Prerequisites
- JDK 17+
- Node.js 16+ (for Scala.js)
- sbt 1.10.11+

```shell
# Clone the repository
git clone https://github.com/nickghignatti/casimo.git
cd casimo

# Run the application
npm run dev
```

### Testing
```shell
# Run all tests
sbt "+backendJVM/test"

# Run with coverage
sbt "coverage; backendJVM/test; coverageReport"
```

### Code coverage

[![codecov](https://codecov.io/gh/NickGhignatti/casimo/branch/master/graph/badge.svg)](https://codecov.io/gh/NickGhignatti/casimo)

### License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

### Authors
- [Marco Galeri](https://github.com/Fre0Grella)
- [Nicolò Ghignatti](https://github.com/NickGhignatti)
- [Luca Patrignani](https://github.com/luca-patrignani)