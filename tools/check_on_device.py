from pathlib import Path
import subprocess
import argparse
import shutil
import threading
import queue

parser=argparse.ArgumentParser(description='Check AquaVuelo on one connected phone; preserve its saved settings.')
parser.add_argument('--adb',default=shutil.which('adb'))
parser.add_argument('--test-apk',type=Path,required=True,help='Instrumentation APK signed with the same debug certificate as the installed app.')
parser.add_argument('--output',type=Path,required=True,help='Directory for the test report and app-only screenshot.')
args=parser.parse_args()
if not args.adb: parser.error('Pass --adb with the Android SDK platform-tools/adb executable.')
if not args.test_apk.is_file(): parser.error('The instrumentation APK does not exist.')
args.output.mkdir(parents=True,exist_ok=True)
adb=args.adb
package='org.atriadev.aquavuelo'
rows=subprocess.check_output([adb,'devices'],text=True).splitlines()
serials=[line.split()[0] for line in rows if len(line.split())==2 and line.split()[1]=='device' and not line.startswith('emulator-')]
if len(serials)!=1: raise SystemExit('Expected one connected phone.')
base=[adb,'-s',serials[0]]
def run(args,**kwargs): return subprocess.run(base+args,check=True,**kwargs)
saved=subprocess.run(base+['exec-out','run-as',package,'cat','shared_prefs/configuration.xml'],capture_output=True)
if saved.returncode: raise SystemExit('Could not preserve app preferences; device tests were not started.')
installed=False
try:
    install=subprocess.run(base+['install','--no-incremental','-r',str(args.test_apk.resolve())],capture_output=True,text=True,timeout=150)
    print((install.stdout+install.stderr).strip(),flush=True)
    if install.returncode: raise RuntimeError('Temporary test component could not be installed.')
    installed=True
    run(['shell','am','force-stop',package],capture_output=True)
    process=subprocess.Popen(base+['shell','am','instrument','-w','org.atriadev.aquavuelo.test/org.atriadev.aquavuelo.SmokeInstrumentation'],stdout=subprocess.PIPE,stderr=subprocess.STDOUT,text=True)
    events=queue.Queue(); lines=[]
    def collect():
        for line in process.stdout:
            lines.append(line); events.put(line); print(line.rstrip(),flush=True)
        events.put(None)
    reader=threading.Thread(target=collect,daemon=True);reader.start()
    try:
        while True:
            line=events.get(timeout=30)
            if line is None: raise RuntimeError('Instrumentation ended before activity launch.')
            if 'Waiting for shell launch' in line:break
        run(['shell','am','start','-n',package+'/.MainActivity'],capture_output=True,text=True,timeout=15)
        process.wait(timeout=60);reader.join(timeout=2)
    finally:
        if process.poll() is None:process.kill()
        (args.output/'phone-test-result.txt').write_text(''.join(lines),encoding='utf-8')
    if not any('PASS:' in line for line in lines): raise RuntimeError('Instrumentation did not report PASS.')
    screenshot=run(['exec-out','run-as',package,'cat','files/preview.png'],capture_output=True)
    if not screenshot.stdout.startswith(b'\x89PNG\r\n\x1a\n'): raise RuntimeError('App screenshot is not a PNG.')
    (args.output/'AquaVuelo-telefono.png').write_bytes(screenshot.stdout)
finally:
    if installed:
        run(['shell','am','force-stop',package],capture_output=True)
        run(['shell','run-as',package,'tee','shared_prefs/configuration.xml'],input=saved.stdout,capture_output=True)
        run(['shell','am','start','-n',package+'/.MainActivity'],capture_output=True)
        run(['uninstall','org.atriadev.aquavuelo.test'],capture_output=True)
        print('App preferences restored; AquaVuelo reopened; temporary test component removed.',flush=True)
