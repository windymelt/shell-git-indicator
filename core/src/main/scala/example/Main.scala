package dev.capslock.shellgitindicator

import io.AnsiColor._

enum RepositoryStatus:
  override def toString(): String = this match {
    case Clean => s"${GREEN}clean${RESET}"
    case Dirty => s"${YELLOW}dirty${RESET}"
  }
  case Clean, Dirty

enum LocalRemoteStatus:
  override def toString(): String = this match {
    case Synced   => s"${GREEN}synced${RESET}"
    case Ahead    => s"${YELLOW}remote-is-ahead${RESET}"
    case Behind   => s"${YELLOW}remote-is-behind${RESET}"
    case Diverged => s"${RED}remote-is-diverged${RESET}"
  }
  case Synced, Ahead, Behind, Diverged

def formatBranch(br: String): String = br match
  case "main"   => s"${YELLOW}$br${RESET}"
  case "master" => s"${YELLOW}$br${RESET}"
  case _        => br

@main def main(args: String*): Unit =
  val repo = os.call(Seq("git", "rev-parse", "--show-toplevel")).out.text().trim
  val basename = os.Path(repo).last
  val status =
    os.proc("git", "status", "--porcelain").call().out.text().trim
  val statusEnum =
    if status.isEmpty then RepositoryStatus.Clean else RepositoryStatus.Dirty

  val branch =
    os.proc("git", "rev-parse", "--abbrev-ref", "HEAD").call().out.text().trim
  val previousBranch =
    os.proc("git", "rev-parse", "--abbrev-ref", "@{-1}")
      .call(check = false, stderr = os.Pipe)
      .out
      .text()
      .trim
  val previousBranchHR =
    if previousBranch == "@{-1}" then "N/A" else previousBranch

  val localRemoteStatus =
    os.proc(
      "git",
      "rev-list",
      "--left-right",
      "--count",
      s"$branch...origin/$branch", // assumes remote is origin
    ).call()
      .out
      .text()
      .trim
      .split("\t")
      .map(_.toInt) match {
      case Array(0, 0) => LocalRemoteStatus.Synced
      case Array(0, _) => LocalRemoteStatus.Ahead
      case Array(_, 0) => LocalRemoteStatus.Behind
      case _           => LocalRemoteStatus.Diverged
    }

  val shellFormat =
    s"($basename:${formatBranch(branch)} $statusEnum $localRemoteStatus)"

  print(shellFormat)
