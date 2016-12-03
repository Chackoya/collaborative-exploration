zeroRow <- matrix(c(0,0,0),nrow=1)
data <- rbind(zeroRow,read.csv("stats.csv",header=FALSE))
rm(zeroRow)
header <- c("steps","seen","error")
colnames(data) <- header

plot(data[,1],data[,2], type = "l", col = "chartreuse2", lwd=3, xlab = "Steps", ylab = "Percentage",ylim=c(0,100), xlim=c(0,5000))
par(new=TRUE)
plot(data[,1],data[,3], type = "l", col = "brown3", lty=2, lwd=3, xlab = "", ylab = "",ylim=c(0,100), xlim=c(0,5000))
legend(0,95, legend=c("Objects identified", "Error"),col=c("chartreuse2", "brown3"), lty=1:2, cex=0.8)