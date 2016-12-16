library(xtable)

filenames<-dir(pattern="*.csv")

original <-  matrix('', ncol = 5, nrow = 7)
colnames(original) <- c("Map type", "F-%  2500","E-%","F-%   5000","E-%")
originalCorner <- original
clustering <- original
clusteringCorner <- original
reclassification <- original
reclassificationCorner <- original
both <- original
bothCorner <- original

bothCounter <- 0
bothCornerCounter <- 0
clusteringCounter <- 0
clusteringCornerCounter <- 0
reclassificationCounter <- 0
reclassificationCornerCounter <- 0
originalCounter <- 0
originalCornerCounter <- 0

for( i in 1:length(filenames) )
{
  filename<-filenames[i]
  filedata<-read.csv(filename,header=FALSE)

  filename = gsub("2\\+4", " ", filename)
  filename = gsub(".csv", "", filename)
  filename = gsub("_", "", filename)
  
  clusteringFound<-(regexpr('clustering', filename) != -1)
  reclassificationFound<-(regexpr('reclassification', filename) != -1)
  cornerFound<-(regexpr('corner', filename) != -1)
  mapType<-strsplit(filename, '_')[[1]][1]
  
  filename = gsub("clustering", "", filename)
  filename = gsub("reclassification", "", filename)
  filename = gsub("structured", "struct.", filename)
  
  
  found2500<-round(filedata[50,2], digits<-2)
  error2500<-round(filedata[50,3], digits<-2)
  found5000<-round(filedata[100,2], digits<-2)
  error5000<-round(filedata[100,3], digits<-2)
  
  # BOTH RANDOM
  if (clusteringFound && reclassificationFound && !cornerFound) {
    bothCounter<-bothCounter + 1
    both[bothCounter,]<-c(filename, found2500, error2500, found5000, error5000);
  }
  
  # BOTH CORNER
  if (clusteringFound && reclassificationFound && cornerFound) {
    bothCornerCounter<-bothCornerCounter + 1
    bothCorner[bothCornerCounter,]<-c(filename, found2500, error2500, found5000, error5000);
  }

  
  # CLUSTERING RANDOM
  if (clusteringFound && !reclassificationFound && !cornerFound) {
    clusteringCounter<-clusteringCounter + 1
    clustering[clusteringCounter,]<-c(filename, found2500, error2500, found5000, error5000);
  }
  
  # CLUSTERING CORNER
  if (clusteringFound && !reclassificationFound && cornerFound) {
    clusteringCornerCounter<-clusteringCornerCounter + 1
    clusteringCorner[clusteringCornerCounter,]<-c(filename, found2500, error2500, found5000, error5000);
  }
  
  
  # RECLASSIFICATION RANDOM
  if (!clusteringFound && reclassificationFound && !cornerFound) {
    reclassificationCounter<-reclassificationCounter + 1
    reclassification[reclassificationCounter,]<-c(filename, found2500, error2500, found5000, error5000);
  }
  
  # RECLASSIFICATION CORNER
  if (!clusteringFound && reclassificationFound && cornerFound) {
    reclassificationCornerCounter<-reclassificationCornerCounter + 1
    reclassificationCorner[reclassificationCornerCounter,]<-c(filename, found2500, error2500, found5000, error5000);
  }
  
  
  # ORIGINAL RANDOM
  if (!clusteringFound && !reclassificationFound && !cornerFound) {
    originalCounter<-originalCounter + 1
    original[originalCounter,]<-c(filename, found2500, error2500, found5000, error5000);
  }
  
  # ORIGINAL CORNER
  if (!clusteringFound && !reclassificationFound && cornerFound) {
    originalCornerCounter<-originalCornerCounter + 1
    originalCorner[originalCornerCounter,]<-c(filename, found2500, error2500, found5000, error5000);
  }
}

both[4,]<-bothCorner[1,]
both[5,]<-bothCorner[2,]
both[6,]<-bothCorner[3,]
both[7,]<-c("average", round(mean(as.numeric(both[1:6,2])),digits=2),round(mean(as.numeric(both[1:6,3])),digits=2),round(mean(as.numeric(both[1:6,4])),digits=2),round(mean(as.numeric(both[1:6,5])),digits=2))

clustering[4,]<-clusteringCorner[1,]
clustering[5,]<-clusteringCorner[2,]
clustering[6,]<-clusteringCorner[3,]
clustering[7,]<-c("average", round(mean(as.numeric(clustering[1:6,2])),digits=2),round(mean(as.numeric(clustering[1:6,3])),digits=2),round(mean(as.numeric(clustering[1:6,4])),digits=2),round(mean(as.numeric(clustering[1:6,5])),digits=2))

reclassification[4,]<-reclassificationCorner[1,]
reclassification[5,]<-reclassificationCorner[2,]
reclassification[6,]<-reclassificationCorner[3,]
reclassification[7,]<-c("average", round(mean(as.numeric(reclassification[1:6,2])),digits=2),round(mean(as.numeric(reclassification[1:6,3])),digits=2),round(mean(as.numeric(reclassification[1:6,4])),digits=2),round(mean(as.numeric(reclassification[1:6,5])),digits=2))

original[4,]<-originalCorner[1,]
original[5,]<-originalCorner[2,]
original[6,]<-originalCorner[3,]
original[7,]<-c("average", round(mean(as.numeric(original[1:6,2])),digits=2),round(mean(as.numeric(original[1:6,3])),digits=2),round(mean(as.numeric(original[1:6,4])),digits=2),round(mean(as.numeric(original[1:6,5])),digits=2))

matrices = list(both, clustering, reclassification, original)
captions = c("Test results with reclassification and clustering","Test results with clustering","Test results with reclassification","Test results without reclassification or clustering")
labels = c("tab:exp5-both","tab:exp5-clustering","tab:exp5-reclassification","tab:exp5-original");
for( i in 1:length(matrices)) {
  cat("\n\n")
  data = matrices[[i]]
  table <- xtable(data,caption = captions[i], label=labels[i])
  align(table) <- "cc|cc|cc"
  print(table,include.rownames=FALSE, hline.after=c(0,6))
  cat("\n\n")
}
  


