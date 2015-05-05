/*The clustering results are saved in 2 arrays:                 */
/*                 1. a returned array of size [K][N] indicating the center  */
/*                    coordinates of K clusters                              */
/*                 2. membership[N] stores the cluster center ids, each      */
/*                    corresponding to the cluster a data object is assigned */
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include <mpi.h>
#include "kmeans.h"


/* square of Euclid distance between two multi-dimensional points            */
__inline static
float euclid_dist_2(int    numdims,  /* no. dimensions */
                    float *coord1,   /* [numdims] */
                    float *coord2)   /* [numdims] */
{
    int i;
    float ans=0.0;

    for (i=0; i<numdims; i++)
        ans += (coord1[i]-coord2[i]) * (coord1[i]-coord2[i]);

    return(ans);
}

/*----< find_nearest_cluster() >---------------------------------------------*/
__inline static
int find_nearest_cluster(int     numClusters, 
                         int     numCoords,   
                         float  *object,      /* [numCoords] */
                         float **clusters)    /* [numClusters][numCoords] */
{
    int   index, i;
    float dist, min_dist;

    index    = 0;
    min_dist = euclid_dist_2(numCoords, object, clusters[0]);

    for (i=1; i<numClusters; i++) {
        dist = euclid_dist_2(numCoords, object, clusters[i]);
        /* no need square root */
        if (dist < min_dist) { /* find the min and its array index */
            min_dist = dist;
            index    = i;
        }
    }
    return(index);
}

int mpi_kmeans(float    **objects,     /* in: [numObjs][numCoords] */
               int        numCoords,   
               int        numObjs,     
               int        numClusters,  
               float      threshold,   /* % objects change membership */
               int       *membership,  /* out: [numObjs] membership of points with parent cluster */
               float    **clusters,    /* out: [numClusters][numCoords] */
               MPI_Comm   comm)        /* MPI communicator */
{
    int      i, j, rank, index, loop=0, total_numObjs , done=1;
    int     *newClusterSize; /* [numClusters]: no. objects assigned in each
                                new cluster */
    int     *clusterSize;    /* [numClusters]: temp buffer for Allreduce */
    float    no_of_changes;          /* % of objects change their clusters */
    float    no_of_changes_tmp;
    float  **newClusters;    /* [numClusters][numCoords] */
    extern int _debug;
    float **temp1;
    float result = 0.0;
    int k=0;
    
    

    if (_debug) MPI_Comm_rank(comm, &rank);

    /* initialize membership[] */
    for (i=0; i<numObjs; i++) membership[i] = -1;

    /* need to initialize newClusterSize and newClusters[0] to all 0 */
    newClusterSize = (int*) calloc(numClusters, sizeof(int));
    assert(newClusterSize != NULL);
    clusterSize    = (int*) calloc(numClusters, sizeof(int));
    assert(clusterSize != NULL);

    newClusters    = (float**) malloc(numClusters *            sizeof(float*));
    assert(newClusters != NULL);
    newClusters[0] = (float*)  calloc(numClusters * numCoords, sizeof(float));
    assert(newClusters[0] != NULL);
    for (i=1; i<numClusters; i++)
        newClusters[i] = newClusters[i-1] + numCoords;

    MPI_Allreduce(&numObjs, &total_numObjs, 1, MPI_INT, MPI_SUM, comm);
    if (_debug) printf("%2d: numObjs=%d total_numObjs=%d numClusters=%d numCoords=%d\n",rank,numObjs,total_numObjs,numClusters,numCoords);

    do {
    	done = 1;
        double curT = MPI_Wtime();
        no_of_changes = 0.0;
        for (i=0; i<numObjs; i++) {
            /* find the array index of nearest cluster center */
            index = find_nearest_cluster(numClusters, numCoords, objects[i],
                                         clusters);

            /*$$$$$$ if membership changes, increase no_of_changes by 1 */
            if (membership[i] != index) no_of_changes += 1.0;
			
					
			
            /* assign the membership to object i */
            membership[i] = index;

            /* update new cluster centers : sum of objects located within */
            newClusterSize[index]++;
            for (j=0; j<numCoords; j++)
                newClusters[index][j] += objects[i][j];
            
        }

		temp1    = (float**) malloc(numClusters * sizeof(float*));	
		for(i=0;i<5;i++)
		{
			temp1[i] = (float*)  calloc(numClusters * numCoords, sizeof(float));	
			temp1[i][0]=clusters[i][0];
			temp1[i][1]=clusters[i][1];
		}

        /* sum all data objects in newClusters */

        MPI_Allreduce(newClusters[0], clusters[0], numClusters*numCoords,
                      MPI_FLOAT, MPI_SUM, comm);
        MPI_Allreduce(newClusterSize, clusterSize, numClusters, MPI_INT,
                      MPI_SUM, comm);

        /* average the sum and replace old cluster centers with newClusters */
        for (i=0; i<numClusters; i++) {
            for (j=0; j<numCoords; j++) {
                if (clusterSize[i] > 1)
                    clusters[i][j] /= clusterSize[i];
                newClusters[i][j] = 0.0;   /* set back to 0 */
            }
            newClusterSize[i] = 0;   /* set back to 0 */
        }

       	for( k=0;k<5 && done == 1;k++)
       	{
       		result = euclid_dist_2(2,temp1[k],clusters[k]);
       		if(result > 0.04)
       			done = 0;
       	}
       		
            
        MPI_Allreduce(&no_of_changes, &no_of_changes_tmp, 1, MPI_FLOAT, MPI_SUM, comm);
        no_of_changes = no_of_changes_tmp / total_numObjs;

        if (_debug) {
            double maxTime;
            curT = MPI_Wtime() - curT;
            MPI_Reduce(&curT, &maxTime, 1, MPI_DOUBLE, MPI_MAX, 0, comm);
            if (rank == 0) printf("%2d: loop=%d time=%f sec\n",rank,loop,curT);
        }
        free(temp1);
    }while((done == 0) && (loop++ < 10000));
    
    
    //printf("\nresult is %f and done is %d\n",result,loop);

    if (_debug && rank == 0) printf("%2d: no_of_changes=%f threshold=%f loop=%d\n",rank,no_of_changes,threshold,loop);

    free(newClusters[0]);
    free(newClusters);
    free(newClusterSize);
    free(clusterSize);

    return 1;
}

