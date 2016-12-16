original = filedata<-read.csv("structured_2+4_corner.csv",header=FALSE)
final = filedata<-read.csv("structured_2+4_corner_clustering_reclassification.csv",header=FALSE)

originalFound = c(original[20,2],original[40,2],original[60,2],original[80,2],original[100,2])
originalError = c(original[20,3],original[40,3],original[60,3],original[80,3],original[100,3])

finalFound = c(final[20,2],final[40,2],final[60,2],final[80,2],final[100,2])
finalError = c(final[20,3],final[40,3],final[60,3],final[80,3],final[100,3])



