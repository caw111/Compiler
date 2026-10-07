"""Run with Python from any directory; requires javac and java on PATH."""
from pathlib import Path
import subprocess
import tempfile

code = Path(__file__).resolve().parents[1]
with tempfile.TemporaryDirectory(prefix="compiler-error-order-") as temporary:
    work = Path(temporary)
    subprocess.run(
        ["javac", "-encoding", "UTF-8", "-d", str(work),
         *map(str, (code / "src").rglob("*.java"))],
        check=True, capture_output=True, text=True,
    )
    cases = [
        ("int main(){\nint a=1\nif(a & 1);\nreturn 0;}", "2 i\n3 a\n"),
        ("int main(){\nint a=1\n" + "\n" * 7 + "if(a & 1);\nreturn 0;}", "2 i\n10 a\n"),
        ("int main(){\nif(1 | 0);\nreturn 0;}", "2 a\n"),
        ("int main(){return 0;}", ""),
    ]
    for source, expected in cases:
        (work / "testfile.txt").write_text(source, encoding="utf-8")
        result = subprocess.run(
            ["java", "-cp", str(work), "Compiler"], cwd=work,
            check=True, capture_output=True, text=True, timeout=10,
        )
        assert not result.stderr, result.stderr
        actual = (work / "error.txt").read_text(encoding="utf-8")
        assert actual == expected, (expected, actual)
        output = (work / "parser.txt").read_text(encoding="utf-8")
        assert (output == "") if expected else ("<CompUnit>" in output)
print("4 error output checks passed.")
