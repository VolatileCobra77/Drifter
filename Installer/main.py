import threading
import tkinter as tk
from tkinter import ttk
import requests
import platform
import os
import zipfile
import subprocess
import getpass
from urllib.parse import urlparse
import shutil
from tkinter import messagebox, filedialog
import math
import time

global installDirectory
installDirectory = ""
username = getpass.getuser()
gitURL = "https://api.github.com/repos/VolatileCobra77/Drifter/releases/latest"
operatingSystem = platform.system().lower()
print(operatingSystem)

if operatingSystem == "windows":
    print("Windows detected")
    installDirectory = f"C:/Users/{username}/Drifter"
    if not os.path.exists(installDirectory):
        os.makedirs(installDirectory)
    print(installDirectory)
    print(os.path.isdir(installDirectory))
elif operatingSystem == "linux":
    print("linux detected")
    installDirectory = f"/home/{username}/Drifter"
    if not os.path.exists(installDirectory):
        os.makedirs(installDirectory)
    print(installDirectory)
    print(os.path.isdir(installDirectory))

cancel_download = False
def set_cancel_download(progress_window):
    global cancel_download
    cancel_download = True
    progress_window.destroy()

def download_with_progress(url, save_path, progress_var, progress_str:tk.Label, progress_window):
    global cancel_download
    try:
        response = requests.get(url, stream=True)
        response.raise_for_status()
        total_size = int(response.headers.get('content-length', 0))
        downloaded_size = 0
        
        start_time = time.time()
        with open(save_path, 'wb') as file:
            for chunk in response.iter_content(chunk_size=1024):
                if cancel_download:
                    messagebox.showinfo("Download Cancelled", "The download has been cancelled.")
                    file.close()
                    os.remove(save_path)
                    progress_window.destroy()
                    return
                if chunk:
                    file.write(chunk)
                    elapsed_time = time.time() - start_time
                    download_speed = downloaded_size / elapsed_time if elapsed_time > 0 else 0
                    remaining_time = (total_size - downloaded_size) / download_speed if download_speed > 0 else 0
                    eta = time.strftime("%H:%M:%S", time.gmtime(remaining_time))
                    downloaded_size += len(chunk)
                    progress_var.set(downloaded_size / total_size * 100)
                    progress_str.config(text=f"Downloaded {downloaded_size} of {total_size} bytes. {math.floor(downloaded_size / total_size * 100)}% done- ETA: {eta}")
                    progress_window.title(f"Downloading... {math.floor(downloaded_size / total_size * 100)}% - ETA: {eta}")
                    progress_window.update_idletasks()

        progress_window.destroy()
        messagebox.showinfo("Download Complete", "Drifter was downloaded successfully, installing")
        success = True
    except requests.exceptions.RequestException as e:
        print(f"Error downloading file: {e}")
        progress_window.destroy()
        messagebox.showerror("Download Error", f"Error downloading file: {e}")
    installVersion(operatingSystem, save_path, url)

def downloadVersion(OperatingSystem, installPath):
    if messagebox.askyesno("Downloading new Version", "Installing new drifter instance, Continue?"):
        if not installPath:
            messagebox.showerror("ERROR: No Install directory specified", "ERROR: No Install directory specified, please select one to continue")
            return
        response = requests.get(gitURL)
        if response.status_code == 200:
            release_data = response.json()
            global downloadURL
            downloadURL = None
            if OperatingSystem == "linux":
                for asset in release_data['assets']:
                    url = asset["browser_download_url"]
                    if str(url).endswith(".tar.gz"):
                        downloadURL = url
            elif OperatingSystem == "windows":
                for asset in release_data['assets']:
                    url = asset["browser_download_url"]
                    if str(url).endswith(".zip"):
                        downloadURL = url
        if downloadURL:
            archive = str(downloadURL).split('/')[-1]
            archive_path = os.path.join(installDirectory, archive)
            
            # Create progress window
            progress_window = tk.Toplevel(root,)
            progress_window.title("Downloading")
            tk.Label(progress_window, text="Downloading...").pack(pady=10)
            progress_var = tk.DoubleVar()
            progress_bar = ttk.Progressbar(progress_window, variable=progress_var, maximum=100)
            progress_txt = tk.Label(progress_window, text=f"Downloading 0 of 0 bytes. 0% done")
            progress_txt.pack(pady=10, padx=5, fill=tk.X)
            progress_bar.pack(pady=10, padx=20, fill=tk.X)
            cancel_button = tk.Button(progress_window, text="Cancel", command=lambda: set_cancel_download(progress_window))
            cancel_button.pack(pady=10)

            progress_window.geometry("450x200")

            # Start download in a new thread
            download_thread = threading.Thread(target=download_with_progress, args=(downloadURL, archive_path, progress_var, progress_txt, progress_window))
            download_thread.start()

def delete_contents(directory, exclude=[]):
    """
    Deletes all contents of the specified directory except for items in the exclude list.

    Args:
    - directory (str): Path to the directory whose contents need to be deleted.
    - exclude (list): List of filenames or folder names to exclude from deletion.
    """
    try:
        for item in os.listdir(directory):
            item_path = os.path.join(directory, item)
            if item not in exclude:
                if os.path.isdir(item_path):
                    shutil.rmtree(item_path)
                else:
                    os.remove(item_path)
    except Exception as e:
        print(f"Error deleting contents of {directory}: {e}")

def installVersion(OperatingSystem, installPath, downloadURL):
    if messagebox.askyesno("Installing new Version", "Caution! This will completely overwrite all game data, excluding save files. Continue?"):
        
        if not installPath:
            messagebox.showerror("ERROR: No Install directory specified", "ERROR: No Install directory specified, please select one to continue")
            return
        if downloadURL:
            
            archive = str(downloadURL).split('/')[-1]
            delete_contents(installDirectory, ["keep", archive])    
            if OperatingSystem == "windows":
                version = getLatestVersion()
                
                if zipfile.is_zipfile(installDirectory+"/"+archive):
                    with zipfile.ZipFile(installDirectory+"/"+archive, 'r') as zip_ref:
                        zip_ref.extractall(installDirectory)
                    move_directory_contents(installDirectory +"/windows/", installDirectory)
                    os.removedirs(installDirectory+"/windows/")
                    os.remove(installDirectory+"/"+archive)
                    with open(installDirectory+"/version.txt", 'w') as f:
                        f.write(getLatestVersion())
                    messagebox.showinfo("Install Complete", "Drifter was successfully installed to "+installDirectory)
                        
def move_directory_contents(src_dir: str, dst_dir: str) -> None:
    if not os.path.exists(dst_dir):
        os.makedirs(dst_dir)

    for item in os.listdir(src_dir):
        src_item = os.path.join(src_dir, item)
        dst_item = os.path.join(dst_dir, item)
        
        try:
            shutil.move(src_item, dst_item)
            print(f"Moved {src_item} to {dst_item}")
        except Exception as e:
            print(f"Error moving {src_item} to {dst_item}: {e}")                        


def getVersion(versionLabel:tk.Label = None):
    
    try:
        with open(installDirectory + "\\version.txt", 'r') as f:
            version = f.readline()
            f.close()
            if versionLabel:
                versionLabel.config(text="Drifter Version: "+ version)
            return version
    except Exception as e:
        print(e)
        #messagebox.showerror("Version.txt not present, are you sure the game is installed?")
        if versionLabel:
            versionLabel.config(text="Drifter Version: None")
        return "None"

def getLatestVersion():
    response = requests.get(gitURL)
    if response.status_code == 200:
        release_data = response.json()
        return str(release_data["tag_name"])

def selectFolder(folderLabel: tk.Label):
    global installDirectory
    oldInstallDirectory = installDirectory
    installDirectory = filedialog.askdirectory(initialdir=oldInstallDirectory, mustexist=True, title="Select an install path for Drifter")
    if installDirectory:
        if messagebox.askyesno("Selected "+installDirectory, "You selected "+installDirectory+" Confirm?"):
            folderLabel.config(text="Drifter Directory: "+installDirectory)
            return installDirectory
        else:
            installDirectory = oldInstallDirectory
            return installDirectory
    else:
        installDirectory = oldInstallDirectory
        return installDirectory

def launchDrifter():
    try:
        if operatingSystem == "windows":
            os.startfile(installDirectory+"/Drifter.exe")
        elif operatingSystem == "linux":
            os.startfile(installDirectory+"/Drifter")
    except Exception as e:
        print(f"Exception {e}")
        messagebox.showerror("ERROR: An error occured launching the game!", f"ERROR: An Error occured while launching the game!{e}")

def checkForUpdates(versionLabel):
    latestVersion = getLatestVersion()
    currentVersion = getVersion()

    print(latestVersion)
    print(currentVersion)

    latestVersionList = latestVersion.split('.')
    currentVersionList = currentVersion.split('.')

    temp = latestVersionList[0].split('-')

    latestVersionList.pop(0)

    latestVersionList = temp + latestVersionList

    temp2 = currentVersionList[0].split('-')

    currentVersionList.pop(0)

    currentVersionList = temp2 + currentVersionList

    print(latestVersionList)
    print(currentVersionList)

    latestVersionNum = 0
    currentVersionNum = 0

    for versionNum in latestVersionList:
        if versionNum == 'Beta':
            versionNum = 0
        elif versionNum == 'Alpha':
            versionNum = 1
        elif versionNum == 'Release':
            versionNum = 2
        latestVersionNum += int(versionNum)
    
    for versionNum in currentVersionList:
        if versionNum == 'Beta':
            versionNum = 0
        elif versionNum == 'Alpha':
            versionNum = 1
        elif versionNum == 'Release':
            versionNum = 2
        currentVersionNum += int(versionNum)

    if latestVersionNum > currentVersionNum:
        if messagebox.askyesno("Update Avalible", "There is an update avalible, download and install it?"):
            downloadVersion(operatingSystem, installDirectory)
            
            return
        else:
            return
    else:
        messagebox.showinfo("No Updates", "No Updates avalible! You are on the latest version!")


def openDrifterFolder():
    print(installDirectory)
    subprocess.Popen(f'explorer "{installDirectory}"')

root = tk.Tk()
root.title("Drifter Updater")
root.geometry("270x310")

drifterVersionLabel = tk.Label(root, text="Drifter Version: "+ getVersion())
drifterVersionLabel.pack(pady=10)

folderLabel = tk.Label(root, text="Drifter Directory: " + installDirectory)
folderLabel.pack(pady=10)

selectFolderButton = tk.Button(root, text="Select Folder", command=lambda:selectFolder(folderLabel))
selectFolderButton.pack(pady=10)

updateButton = tk.Button(root, text="Download latest version", command=lambda:downloadVersion(operatingSystem, installDirectory))
updateButton.pack(pady=10)

checkForUpdate = tk.Button(root, text="Check for updates", command=lambda:checkForUpdates(drifterVersionLabel))
checkForUpdate.pack(pady=10)

openFolder = tk.Button(root, text="Open Drifter Folder", command=lambda:openDrifterFolder())
openFolder.pack(pady=10)

launchButton = tk.Button(root, text="Launch Drifter", command=lambda:launchDrifter())
launchButton.pack(pady=10)

root.mainloop()