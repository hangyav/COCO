macro "COCO batch" {
source = getDirectory("Choose source directory ");
target   = getDirectory("Choose target directory ");
list = getFileList(source);
setBatchMode(true);
for(i=0; i<list.length; i++){
  showProgress(i-1, list.length);
  showStatus("COCO batch "+i+" of "+list.length+"("+list[i]+")");
  path = source+list[i];
  open(path);
  run("COCO ");
  path=target+"COCO_"+list[i];
  save(path);
  close();
}
showMessage("COCO batch", "Your COCO batch is finished!");
showStatus("");
}
