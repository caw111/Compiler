"""Check the platform sample and main's scope numbering in temporary files."""
from pathlib import Path
import subprocess
import tempfile

code = Path(__file__).resolve().parents[1]
sample = r'''const int year = 2026, month = 9;
int day;
int getDay(){
    static int day;
    day = get_int();
    return day;
}
int fun(){
    return 12345;
}
int main(){
    day = getDay();
    printf("Tody is %d-%d-%d\n", year, month, day);
    {
        int length = fun();
        if(length > 4){
            printf("%d\n", length);
        }
    }
    return 0;
}
'''
cases = [
    (sample, "1 year ConstInt\n1 month ConstInt\n1 day Int\n"
             "1 getDay IntFunc\n1 fun IntFunc\n2 day StaticInt\n5 length Int\n"),
    ("int main(){int x;{char c;}return 0;}", "2 x Int\n3 c Char\n"),
    ("int main(){return 0;}", ""),
]

with tempfile.TemporaryDirectory(prefix="symbol-output-") as temporary:
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
        assert (work / "error.txt").read_text(encoding="utf-8") == ""
        actual = (work / "symbol.txt").read_text(encoding="utf-8")
        assert actual == expected, (source, expected, actual)

print(f"{len(cases)} exact symbol output checks passed.")
