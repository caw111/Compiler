"""Run with Python; compile and run Compiler in temporary directories."""
from pathlib import Path
import subprocess
import tempfile

code = Path(__file__).resolve().parents[1]
cases = [
    ("int main(){\nconst int x='a';\nreturn 0;}", "2 e\n"),
    ("int main(){\nconst char a[2]={1,2};\nreturn 0;}", "2 e\n"),
    ('int main(){\nint a[2]="ab";\nreturn 0;}', "2 e\n"),
    ('int main(){\nconst int a[2]="ab";\nreturn 0;}', "2 e\n"),
    ('int main(){\nint a="ab";\nreturn 0;}', "2 e\n"),
    ('int main(){char a[3]="ab";const char b[3]="cd";return 0;}', ""),
    ('int main(){char a="a";const char b="b";return 0;}', ""),
    ("int main(){const int a=1;const int b[2]={a,2};return 0;}", ""),
    ("int main(){const char a='a';const char b[2]={a,'b'};return 0;}", ""),
    ("int main(){const int a=(int)'a';char b=(char)1;return 0;}", ""),
    ("int main(){\nconst int a=missing;\nreturn 0;}", "2 c\n"),
    ("int main(){\nint a[2]={\n'a',\nmissing\n};\nreturn 0;}", "3 e\n4 c\n"),
    ("int main(){\nconst int a[2]={\n'a',\n'b'\n};\nreturn 0;}", "3 e\n4 e\n"),
    ("int main(){\nconst int a=1+'a';\nreturn 0;}", "2 e\n"),
    ("int f(){\nreturn;\n}\nint main(){return 0;}", "2 e\n"),
    ("char f(){\nreturn;\n}\nint main(){return 0;}", "2 e\n"),
    ("void f(){return;}int main(){return 0;}", ""),
    ("void f(){\nreturn 1;\n}\nint main(){return 0;}", "2 f\n"),
    ("int f(){\n}\nint main(){return 0;}", "2 g\n"),
    ("int main(){\nif(!'a' == 0);\nreturn 0;}", ""),
    ("int main(){\nif(!'a' == 'b');\nreturn 0;}", "2 e\n"),
    ("int main(){char a=-'a';char b=+'b';return 0;}", ""),
    ("int main(){\nint a[2];\nif(!a);\nreturn 0;}", "3 e\n"),
    ('int main(){char s[3]="ab";put_str(s);return 0;}', ""),
    ('int main(){char s[3]="ab";put_string(s);return 0;}', ""),
    ('int main(){\nput_str();\nreturn 0;}', "2 d\n"),
    ('int main(){int a[2];\nput_str(a);\nreturn 0;}', "2 e\n"),
]

with tempfile.TemporaryDirectory(prefix="semantic-init-") as temporary:
    work = Path(temporary)
    subprocess.run(
        ["javac", "-encoding", "UTF-8", "-d", str(work),
         *map(str, (code / "src").rglob("*.java"))],
        check=True, capture_output=True, text=True,
    )
    for source, expected in cases:
        (work / "testfile.txt").write_text(source, encoding="utf-8")
        result = subprocess.run(
            ["java", "-cp", str(work), "Compiler"], cwd=work,
            check=True, capture_output=True, text=True, timeout=10,
        )
        assert not result.stderr, result.stderr
        actual = (work / "error.txt").read_text(encoding="utf-8")
        assert actual == expected, (source, expected, actual)

print(f"{len(cases)} semantic regression checks passed.")
