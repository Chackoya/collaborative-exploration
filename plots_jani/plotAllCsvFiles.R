filenames = dir(pattern="*.csv")
for( i in 1:length(filenames) )
{
  zeroRow <- matrix(c(0,0,0),nrow=1)
  data <- rbind(zeroRow,read.csv(filenames[i],header=FALSE))
  rm(zeroRow)
  header <- c("steps","seen","error")
  colnames(data) <- header
  
  imageFileName = sub("csv", "png", filenames[i]);
  png(filename=imageFileName, width = 640, height = 593, units = "px", pointsize = 20);  
  
  plot(data[,1],data[,2], type = "l", col = "chartreuse2", lwd=6, xlab = "Steps", ylab = "Percentage",ylim=c(0,100), xlim=c(0,5000))
  par(new=TRUE)
  plot(data[,1],data[,3], type = "l", col = "brown3", lty=2, lwd=6, xlab = "", ylab = "",ylim=c(0,100), xlim=c(0,5000))
  legend(0,95, legend=c("Objects identified", "Error"),col=c("chartreuse2", "brown3"), lty=1:5, cex=0.9, lwd=3)
  dev.off()
}