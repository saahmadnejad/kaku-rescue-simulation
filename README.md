# Kaku — RoboCup Rescue Simulation Agent Team

Agent team for the RoboCup Rescue Simulation (RCRS) Agent Competition, built on the Agent Development Framework (ADF).

- League: <https://rescuesim.robocup.org/competitions/agent-simulation-competition/>
- ADF core: <https://github.com/roborescue/adf-core-java>
- Server (not part of this repo): <https://github.com/roborescue/rcrs-server>

## Team

- **Team name**: `kaku`
- Agents developed: Fire Brigade, Police Force, Ambulance Team (+ centres)

## 1. Pre-requisites

* Git
* OpenJDK Java 21
* Gradle (or use bundled `./gradlew`)

## 2. Build

```bash
$ ./gradlew clean build
```

## 3. Run

First start the `rcrs-server` (see its repo for instructions), e.g.:

```bash
$ cd rcrs-server/scripts
$ bash start.sh -m ../maps/kobe/map -c ../maps/kobe/config -g
```

Then connect this team:

```bash
$ ./launch.sh -all
# or directly:
$ gradle launch --args="adf.impl.DefaultLoader -all"
```

Options: `-t [FB],[FS],[PF],[PO],[AT],[AC]` agent counts, `-all` = all, `-h <host>` server host.

## 4. Development

Team code lives in `src/main/java/` (module implementations: path planning, target selection, clustering, communication). Framework module interfaces come from `adf-core-java` (Maven dependency). See `docs/adf-manual.adoc` for the ADF manual.

## 5. Testing — Mandatory

Every contribution MUST come with unit tests:

- Framework: **JUnit 5** (`gradle test`)
- Structure every test with **AAA** (Arrange, Act, Assert) and name it with the
  **given / when / then** principle:
  - Method name describes the scenario: `longPathReturnsThirdToLast()`
  - Inside the test, mark the phases with `// given`, `// when`, `// then`
- Test package mirrors main package: `src/test/java/io/donbee/kaku/rescue/...`
- Pure logic goes into testable (static/package-private) methods; framework-bound
  code gets config-consistency or contract tests.
- A PR without tests for new/changed behaviour will not be merged.

Example:

```java
@Test
@DisplayName("long path returns node three steps before the end")
void longPathReturnsThirdToLast() {
    // given
    List<EntityID> path = ids(1, 2, 3, 4, 5, 6, 7);

    // when
    EntityID target = SampleSearch.targetFromPath(path);

    // then
    assertEquals(new EntityID(5), target);
}
```

## 6. Contribution Rules

1. **Conventional Commits** (see section below) — no exceptions.
2. **Tests for everything** — new behaviour, bug fixes, config changes.
   `gradle test` must pass before every push.
3. One logical change per commit; keep commits small and reviewable.
4. Code style: follow the existing format (Google Java Style, 2-space indent).
5. Package root for all team code: `io.donbee.kaku.rescue`.

## 7. Commit Convention — Conventional Commits

This project follows the [Conventional Commits](https://www.conventionalcommits.org/) standard. Every commit message MUST be structured as:

```
<type>(<optional scope>): <short imperative summary>

[optional body]

[optional footer(s)]
```

### Types

| Type | Use |
|---|---|
| `feat` | New feature or agent behaviour |
| `fix` | Bug fix |
| `docs` | Documentation only |
| `style` | Formatting, no logic change |
| `refactor` | Code change, no feature/fix |
| `test` | Adding/correcting tests |
| `chore` | Tooling, build, deps, CI |
| `perf` | Performance improvement |

### Examples

```
feat(tactics): add fire clustering to FireBrigade target selection
fix(path): correct A* cost for blocked roads
docs(readme): document conventional commit standard
chore(deps): bump adf-core-java version
```

Rules:
- Subject line: imperative mood, lowercase, no period, max ~72 chars
- Scope is optional and names the affected module (e.g. `tactics`, `path`, `cluster`, `comms`)

## 8. Support

To report a bug or suggest improvements, open an issue on GitHub.
