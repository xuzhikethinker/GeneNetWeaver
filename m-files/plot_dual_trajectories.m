function plot_dual_trajectories(networkName, tagTs1, tagTs2, genes, experiments, mode)
% plot_dual_trajectories   Plot two trajectories datasets
%
%   Usage: >> plot_dual_trajectories(networkName, tagTs1, tagTs2)
%             - networkName : root name of the network
%             - tagTs1 : tag to identify the first trajectories dataset
%             - tagTs2 : tag to identify the second trajectories dataset
%             - genes : genes to display, e.g. [1:5,6,8]
%             - experiments : experiments to display, e.g. [1:4,7,8,9]
%             - mode : 'dual' plot both time-series, ~'dual' only the first
%                      time-series
%
%   Note: files extension must be .tsv
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
% June 11, 2009


%close all;

%% ---------------------------------------------------------------- Options

% Extension of the files
extension = '.tsv';

% Size of the subplots
numRows = 5;
numCols = 2;

% plot on log scale
plotLog = 0;

% adjust subplot layout
adjustSubplots = 0;

% Graph x label
xLabel = 'Time';
% Graph y label
yLabel = 'Expression level';


%% ------------------------------------------------------------------- Main

if nargin < 6
   mode = 'dual';
end


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
[numGenes,labelsStr] = extractGeneLabels(fin1);
[timescale,data1] = extractTsData(fin1, numGenes);

if strcmp(mode, 'dual')
    fin2 = openfile(filename2);
    [numGenes,labelsStr] = extractGeneLabels(fin2);
    [timescale,data2] = extractTsData(fin2, numGenes);
end


if (plotLog)
    data1 = log10(data1);
    if strcmp(mode, 'dual')
        data2 = log10(data2);
    end
end


% number of time series
numTimeSeries = size(data1,1) / numGenes;

disp(['Total number of genes: ' num2str(numGenes)]);
disp(['Total number of time-series experiments: ' num2str(numTimeSeries)]);


% check which gene trajectories are wished
if nargin < 4 || isempty(genes)
   genes = 1:numGenes;
end

% check which time-serie experiments are wished
if nargin < 5 || isempty(experiments)
   experiments = 1:numTimeSeries;
end


% remove the double entries and sort the indexes in ascending order
expIndexes = sort(unique(experiments));
trajIndexes = sort(unique(genes));


disp(['Number of genes to display: ' num2str(size(trajIndexes,2))]);
disp(['Number of time-series experiments to display: ' num2str(size(expIndexes,2))]);


% do not display more plots than specified above
if size(expIndexes,2) > numRows*numCols
   disp(['Can display max ' num2str(numRows*numCols) ' experiments at the the time'])
   return
end


% adjust the number of rows with the number of experiments wished
if adjustSubplots == 1
    numRows = floor(size(expIndexes,1)/numCols);
    numRows = numRows + mod(size(expIndexes,1),numCols);
    % remove eventual unsued columns
    if size(expIndexes,1) < numCols
        numCols = size(expIndexes,1);
    end
end


figure
co = get(0, 'DefaultAxesColorOrder');
for i = 1:size(expIndexes,2)
    
    % index of the current time-series experiment
    index = expIndexes(i);
    
    
    subplot(numRows, numCols, i);
    hold on;
    ylim([0 1]);
    %set(gcf, 'Name', ['Time series ' int2str(index)]);
    set(gcf, 'Name', [tagTs1 ' vs. ' tagTs2]);
    title(['Time series ' int2str(index)]);
    
    % get the needed ts data
    timeSeries1 = data1((index-1)*numGenes+1:index*numGenes,:);
    if strcmp(mode, 'dual')
        timeSeries2 = data2((index-1)*numGenes+1:index*numGenes,:);
    end
    
    for j=1:size(trajIndexes,2)
       % index of the current gene trajectory
       index2 = trajIndexes(j);
       
       if strcmp(mode, 'dual')
           a = plot(timescale, [timeSeries1(index2,:);timeSeries2(index2,:)], 'color', co(mod(index2-1,size(co,1))+1,:));
       else
           a = plot(timescale, timeSeries1(index2,:), 'color', co(mod(index2-1,size(co,1))+1,:));
       end
       
       % process gene label and add legend
       text = cell2str(labelsStr(index2));
       text = text(3:size(text,2)-2);
       aGrp = hggroup('DisplayName', ['G' num2str(index2) '=' text]);
       set(a,'Parent',aGrp)
       set(get(get(aGrp,'Annotation'),'LegendInformation'),...
            'IconDisplayStyle','on');
    end
    
    hold off;
    
    % only display the legend for the first time-series experiment
    if i==1
        legend show
    end
   
end

end


%% ========================================================================
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


%% ------------------------------------------------------------------------
% String modifier
% 'Gene1' -> ['Gene1' appendix]
% input is a cellstr array

function input = stringAddAppendix(input, appendix)
    s = size(input, 1);  
    for i=1:s
        input(i) = strcat(input(i),appendix);
    end
end


%% ------------------------------------------------------------------------
% Open the file associated to filename and return the stream.

function fin = openfile(filename)

    fin = fopen(filename,'r');
    if fin < 0
       error(['Could not open ',filename,' for input']);
    end
end


%% ------------------------------------------------------------------------
% Read the file associated to the stream fin and extract all the experi-
% ments. The layout of the output data is a (M X TP) with:
% - M the total number of time series curves
% - TP is the number of time point in the time scale
%
% Hypothesis: All experiments have the same time scale.

function [timescale, data] = extractTsData(fin, numGenes)
    
    disp('Scan time-series file...')
    rawData = fscanf(fin,'%f'); % Load the numerical values into one long
                                % vector
    disp('Extract time-series...')
                                
    % timescale and time-series data are mixed.
    % The output data will not contain the time scale. 
    [timescale, data] = extractTimeScale(rawData, numGenes);
    
    % Set the layout described above for data
    TP = size(timescale,1);
    M = size(data,1)/TP;
    data = reshape(data,TP,M)';
end


%% ------------------------------------------------------------------------
% Extract the time scale from the raw data and remove this same time scale
% from the experimental time series data.
% The time scale is only extract from the first experiment (see above
% hypothesis).

function [timescale, data] = extractTimeScale(rawData, numGenes)

    timescale = rawData(1,1); % add element t0 to timescale
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
    
    disp('Remove timescale from data...');
    data = removeTimeScale(rawData,numGenes);
    data = repartitionInCurves(data,numGenes,size(timescale,1));
end


%% ------------------------------------------------------------------------
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


%% ------------------------------------------------------------------------
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


%% ------------------------------------------------------------------------
% Extract all the gene labels. Output is a cellstr array.

function [numGenes, labelsStr] = extractGeneLabels(fin)

    header = fgetl(fin); % Get the first line, the horizontal header
    maxlen = 0;
    stop = 0;
    numCol = 0;

    while stop == 0
      [next,header] = strtok(header); %#ok<STTOK> %  parse next column label
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
      [next,buffer] = strtok(buffer); %#ok<STTOK> %  parse next column label
      n = j; %  pointer into the label array for next label
      labels(n,1:length(next)) = next; %  append to the labels matrix
    end

    % Conversion labels(char) -> labels(string)
    labelsStr = cellstr(labels);
    labelsStr = labelsStr(2:size(labelsStr,1),:);
    labelsStr = stringRemoveQuotes(labelsStr);
end


%% ------------------------------------------------------------------------
%CELL2STR Convert cell array into evaluable string.
%   B = CELL2STR(C) returns a B such that C = EVAL(B), under the
%   following contraits:
%   - C is composed of numeric arrays or strings.
%   - All of the elements of C are of the same type.
%   - C is a row vector, that is, SIZE(C,1) == 1 and NDIMS(C) = 2.
%
%   See also MAT2STR
%
% (c)2000 by Cris Luengo

function str = cell2str(c)

    if ~iscell(c)

       if ischar(c)
          str = ['''',c,''''];
       elseif isnumeric(c)
          str = mat2str(c);
       else
          error('Illegal array in input.')
       end

    else

       N = length(c);
       if N > 0
          if ischar(c{1})
             str = ['{''',c{1},''''];
             for ii=2:N
                if ~ischar(c{ii})
                   error('Inconsistent cell array');
                end
                str = [str,',''',c{ii},''''];
             end
             str = [str,'}'];
          elseif isnumeric(c{1})
             str = ['{',mat2str(c{1})];
             for ii=2:N
                if ~isnumeric(c{ii})
                   error('Inconsistent cell array');
                end
                str = [str,',',mat2str(c{ii})];
             end
             str = [str,'}'];
          end
       else
          str = '';
       end
    end
end
