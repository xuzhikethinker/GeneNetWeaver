function check_expnoise(networkName)
% check_expnoise   Plot Xnoexpnoise vs. X
%
%   Usage: >> check_expnoise(networkName)
%             - networkName : root name of the network
%
%   Note: files extension must be .tsv
%
% Requires the following files (where ID=networkName):
%
% ID_noexpnoise_knockouts_timeseries.tsv, ID_knockouts_timeseries.tsv,
% ID_noexpnoise_knockdowns_timeseries.tsv, ID_knockdowns_timeseries.tsv,
% ID_noexpnoise_timeseries.tsv, ID_timeseries.tsv,
% ID_noexpnoise_proteins_timeseries.tsv, ID_proteins_timeseries-tsv,
% ID_noexpnoise_proteins_knockouts_timeseries.tsv,
% ID_proteins_knockouts_timeseries.tsv,
% ID_noexpnoise_proteins_knockdowns_timeseries.tsv,
% ID_proteins_knockdowns_timeseries.tsv
%
%
% Thomas Schaffter, Ph.D. Student
% Ecole Polytechnique Federale de Lausanne (EPFL)
% Laboratory of Intelligent Systems (LIS)
% CH-1015 Lausanne, Switzerland
% http://lis.epfl.ch/161219
%
% Daniel Marbach, Ph.D. Student
% Ecole Polytechnique Federale de Lausanne (EPFL)
% Laboratory of Intelligent Systems (LIS)
% CH-1015 Lausanne, Switzerland
% http://lis.epfl.ch/136698
%
% June 12, 2009


figure;
run(networkName, '_noexpnoise_timeseries', '_timeseries');
run(networkName, '_noexpnoise_knockouts_timeseries', '_knockouts_timeseries');
run(networkName, '_noexpnoise_knockdowns_timeseries', '_knockdowns_timeseries');
run(networkName, '_noexpnoise_proteins_timeseries', '_proteins_timeseries');
run(networkName, '_noexpnoise_proteins_knockouts_timeseries', '_proteins_knockouts_timeseries');
run(networkName, '_noexpnoise_proteins_knockdowns_timeseries', '_proteins_knockdowns_timeseries');

xlabel('X noexpnoise');
ylabel('X');

hold off

end


function run(networkName, tagTs1, tagTs2)

    % Extension of the file
    extension = '.tsv';

    filename1 = [networkName tagTs1 extension];
    filename2 = [networkName tagTs2 extension];


    % Open the file and load its content
    % ==================================
    % Found the number of genes and get the header
    % --------------------------------------------

    % 1. Open file.
    % 2. Extract numGenes and the labels of all genes
    % 3. Load time-series numerical data
    fin1 = openfile(filename1);
    fin2 = openfile(filename2);
    [numGenes,labelsStr] = extractGeneLabels(fin1);
    [numGenes,labelsStr] = extractGeneLabels(fin2);
    % data: first line is first time series of first gene, then comes
    % first time series of second gene, etc. for all time series
    [timescale,data1] = extractTsData(fin1, numGenes);
    [timescale,data2] = extractTsData(fin2, numGenes);


    % the number of time series
    numTimeSeries = size(data1,1) / numGenes;

    for i = 0:numTimeSeries-1

        hold on;
        ylim([0 1]);
        set(gcf, 'Name', ['Time series ' int2str(i+1)]);

        timeSeries1 = data1(i*numGenes+1:(i+1)*numGenes,:); % without exp noise
        timeSeries2 = data2(i*numGenes+1:(i+1)*numGenes,:); % with exp noise

        for j=0:size(timeSeries1,1)-1
            plot(timeSeries1(j+1,:), timeSeries2(j+1,:), '.');
        end
    end

end




% =========================================================================
% SUBFUNCTIONS

% String modifier
% "'Gene1'" -> 'Gene1'
function list = stringRemoveQuotes(input)
    s = size(input, 1);
    for i=1:s
        input(i) = regexprep(input(i), '"', '');
    end
    list = input;
end

% -------------------------------------------------------------------------

% String modifier
% 'Gene1' -> ['Gene1' appendix]
% input is a cellstr array
function input = stringAddAppendix(input, appendix)
    s = size(input, 1);  
    for i=1:s
        input(i) = strcat(input(i),appendix);
    end
end

% -------------------------------------------------------------------------

% Open the file associated to filename and return the stream.
function fin = openfile(filename)

    fin = fopen(filename,'r');
    if fin < 0
       error(['Could not open ',filename,' for input']);
    end
end

% -------------------------------------------------------------------------

% Read the file associated to the stream fin and extract all the experi-
% ments. The layout of the output data is a (M X TP) with:
% - M the total number of time series curves
% - TP is the number of time point in the time scale
%
% Hypothesis: All experiments have the same time scale.
function [timescale, data] = extractTsData(fin, numGenes)

    rawData = fscanf(fin,'%f'); % Load the numerical values into one long
                                % vector
                                
    % timescale and time-series data are mixed.
    % The output data will not contain the time scale. 
    [timescale, data] = extractTimeScale(rawData, numGenes);
    
    % Set the layout described above for data
    TP = size(timescale,1);
    M = size(data,1)/TP;
    data = reshape(data,TP,M)';
end

% -------------------------------------------------------------------------

% Extract the time scale from the raw data and remove this same time scale
% from the experimental time series data.
% The time scale is only extract from the first experiment (see above
% hypothesis).
function [timescale, data] = extractTimeScale(rawData, numGenes)

    timescale = rawData(1,1);
    stop = 0;
    
    while stop == 0
        index = size(timescale,1);
        potentialTimePoint = rawData(index*(numGenes+1)+1,1);
        if potentialTimePoint ~= 0
            timescale = [timescale;potentialTimePoint];
        else
            stop = 1;
        end
        

        if (index+1)*(numGenes+1)+1 >  size(rawData,1)
            stop = 1;
        end
    end
    data = removeTimeScale(rawData,numGenes);
    data = repartitionInCurves(data,numGenes,size(timescale,1));
end

% -------------------------------------------------------------------------

% Finally, data is a vector with time series curves one after one.
function correctData = repartitionInCurves(data,numGenes,numTimePoints)

    numExp = size(data,1)/(numGenes*numTimePoints);
    curve = zeros(numTimePoints,1);
    correctData = zeros(numTimePoints,1);
    
    for e=1:numExp
        expDataIndex = (e-1)*numGenes*numTimePoints+1;
        for g=1:numGenes
            geneDataIndex = expDataIndex+(g-1);
            for pt=1:numTimePoints
               pointDataIndex = geneDataIndex+(pt-1)*(numGenes);
               curve(pt) = data(pointDataIndex,1);
            end
            if e ==1 && g==1
               correctData = curve;
            else
               correctData = [correctData;curve];
            end
        end
    end
end

% -------------------------------------------------------------------------

% Remove the time scale of the raw time-series data.
function data = removeTimeScale(rawData,numGenes)

    numRows = size(rawData,1)/(numGenes+1);
    data = rawData(2:numGenes+1,1);
    
    for i=1:numRows-1
        start = i*(numGenes+1)+2;
        finish = start+numGenes-1;
        data = [data;rawData(start:finish,1)];
    end
end

% -------------------------------------------------------------------------

% Extract all the gene labels. Output is a cellstr array.
function [numGenes, labelsStr] = extractGeneLabels(fin)

    header = fgetl(fin); % Get the first line, the horizontal header
    maxlen = 0;
    stop = 0;
    numCol = 0;

    while stop == 0
      [next,header] = strtok(header); %  parse next column label
      nextLen = length(next);
      maxlen = max(maxlen,nextLen); %  find the longest so far
      if nextLen == 0
          stop = 1;
      else
         numCol = numCol+1;
      end
    end

    numGenes = numCol-1;
%     sprintf('Number of genes: %d', numGenes)

    labels = blanks(maxlen);
    frewind(fin); % rewind in preparation for actual reading of labels and data
    buffer = fgetl(fin); %  get next line as a string
    for j=1:numCol
      [next,buffer] = strtok(buffer); %  parse next column label
      n = j; %  pointer into the label array for next label
      labels(n,1:length(next)) = next; %  append to the labels matrix
    end

    % Conversion labels(char) -> labels(string)
    labelsStr = cellstr(labels);
    labelsStr = labelsStr(2:size(labelsStr,1),:);
    labelsStr = stringRemoveQuotes(labelsStr);
end
