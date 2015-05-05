# vim:set ts=8 sw=8 sts=0 noet:

#  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
#    File:         Makefile                                                  */
#    Description:  Makefile for programs running a simple k-means clustering */
#                  algorithm                                                 */
#                                                                            */
#    Author:  Wei-keng Liao                                                  */
#             ECE Department Northwestern University                         */
#             email: wkliao@ece.northwestern.edu                             */
#    Copyright, 2005, Wei-keng Liao                                          */
#                                                                            */
#  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

# Copyright (c) 2005 Wei-keng Liao
# Copyright (c) 2011 Serban Giuroiu
#
# Permission is hereby granted, free of charge, to any person obtaining a copy
# of this software and associated documentation files (the "Software"), to deal
# in the Software without restriction, including without limitation the rights
# to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
# copies of the Software, and to permit persons to whom the Software is
# furnished to do so, subject to the following conditions:
#
# The above copyright notice and this permission notice shall be included in
# all copies or substantial portions of the Software.
#
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
# IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
# FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
# AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
# LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
# OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
# THE SOFTWARE.

# ------------------------------------------------------------------------------

.KEEP_STATE:

all: mpi

DFLAGS      =
OPTFLAGS    = -O -NDEBUG
OPTFLAGS    = -g -pg
INCFLAGS    = -I.
CFLAGS      = $(OPTFLAGS) $(DFLAGS) $(INCFLAGS) -DBLOCK_SHARED_MEM_OPTIMIZATION=1
LDFLAGS     = $(OPTFLAGS)
LIBS        =

# please check the compile to the one you use and the openmp flag
# Here, I am using gcc and its openmp compile flag is -fopenmp
# If icc is used, please us -opnemp
#

CC          = gcc
MPICC       = mpicc

.c.o:
	$(CC) $(CFLAGS) -c $<

H_FILES     = kmeans.h

#------   MPI version -----------------------------------------
MPI_SRC     = mpi_main.c   \
              mpi_kmeans.c \
              mpi_io.c     \
	      file_io.c

MPI_OBJ     = $(MPI_SRC:%.c=%.o)

mpi_main.o: mpi_main.c $(H_FILES)
	$(MPICC) $(CFLAGS) -c $*.c

mpi_kmeans.o: mpi_kmeans.c $(H_FILES)
	$(MPICC) $(CFLAGS) -c $*.c

mpi_io.o: mpi_io.c $(H_FILES)
	$(MPICC) $(CFLAGS) -c $*.c

mpi: mpi_main
mpi_main: $(MPI_OBJ) $(H_FILES)
	$(MPICC) $(LDFLAGS) -o mpi_main $(MPI_OBJ) $(LIBS)

#---------------------------------------------------------------------
clean:
	rm -rf *.o mpi_main \
	       core* .make.state gmon.out     \
               *.cluster_centres *.membership \
               Image_data/*.cluster_centres   \
               Image_data/*.membership        \
               profiles/
